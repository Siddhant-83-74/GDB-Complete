package com.gdb.creditcards.service.impl;

import com.gdb.creditcards.client.PaymentGatewayClient;
import com.gdb.creditcards.constants.CreditCardConstants;
import com.gdb.creditcards.domain.model.CardServiceLimit;
import com.gdb.creditcards.domain.model.CreditCard;
import com.gdb.creditcards.domain.model.CreditCardTransaction;
import com.gdb.creditcards.dto.request.CardTransactionRequest;
import com.gdb.creditcards.dto.request.PayBillRequest;
import com.gdb.creditcards.dto.response.TransactionResponse;
import com.gdb.creditcards.exception.CreditCardException;
import com.gdb.creditcards.mapper.CreditCardMapper;
import com.gdb.creditcards.repository.CreditCardRepository;
import com.gdb.creditcards.repository.CreditCardTransactionRepository;
import com.gdb.creditcards.service.CreditCardService;
import com.gdb.creditcards.service.CreditCardTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreditCardTransactionServiceImpl implements CreditCardTransactionService {

    private final CreditCardRepository cardRepository;
    private final CreditCardTransactionRepository txnRepository;
    private final CreditCardService cardService;
    private final PaymentGatewayClient paymentGatewayClient;

    @Value("${app.rules.high-value-threshold:50000}")
    private BigDecimal highValueThreshold;

    @Value("${app.rules.velocity-count:3}")
    private int velocityCount;

    @Value("${app.rules.velocity-window-minutes:5}")
    private int velocityWindowMinutes;

    @Override
    @Transactional
    public TransactionResponse transact(String cardId, CardTransactionRequest request) {
        CreditCard card = cardService.requireCard(cardId);
        assertActive(card);

        // b.16 international toggle
        if (request.isInternational() && !Boolean.TRUE.equals(card.getInternationalEnabled())) {
            throw new CreditCardException("International transactions are disabled on this card",
                    CreditCardConstants.INTERNATIONAL_DISABLED);
        }

        // b.15 per-channel service limit
        Optional<CardServiceLimit> serviceLimit = cardRepository.findServiceLimit(cardId, request.getChannel());
        if (serviceLimit.isPresent()
                && request.getAmount().compareTo(serviceLimit.get().getPerTxnLimit()) > 0) {
            throw new CreditCardException(
                    "Amount exceeds the " + request.getChannel() + " per-transaction limit",
                    CreditCardConstants.SERVICE_LIMIT_EXCEEDED);
        }

        // b.3 cannot transact more than available credit
        if (request.getAmount().compareTo(card.getAvailableCredit()) > 0) {
            throw new CreditCardException("Transaction amount exceeds available credit limit",
                    CreditCardConstants.LIMIT_EXCEEDED);
        }

        // Determine whether the transaction must be held (b.6 high value, b.7 velocity)
        String holdReason = resolveHoldReason(cardId, request.getAmount());

        CreditCardTransaction txn = CreditCardTransaction.builder()
                .id(UUID.randomUUID().toString())
                .cardId(cardId)
                .type(CreditCardConstants.TXN_PURCHASE)
                .channel(request.getChannel())
                .amount(request.getAmount())
                .merchant(request.getMerchant())
                .international(request.isInternational())
                .status(holdReason != null ? CreditCardConstants.TXN_ON_HOLD : CreditCardConstants.TXN_APPROVED)
                .holdReason(holdReason)
                .build();
        txnRepository.save(txn);

        // Funds are only committed once a transaction is APPROVED (held ones wait
        // for explicit confirmation).
        if (holdReason == null) {
            applyDebit(card, request.getAmount());
        } else {
            log.info("Transaction {} on card ending {} placed ON_HOLD: {}",
                    txn.getId(), card.getCardLast4(), holdReason);
        }
        return CreditCardMapper.toResponse(txn);
    }

    @Override
    @Transactional
    public TransactionResponse confirmHold(String transactionId) {
        CreditCardTransaction txn = txnRepository.findById(transactionId)
                .orElseThrow(() -> new CreditCardException("Transaction not found: " + transactionId,
                        CreditCardConstants.TRANSACTION_NOT_FOUND));

        if (!CreditCardConstants.TXN_ON_HOLD.equals(txn.getStatus())) {
            throw new CreditCardException("Transaction is not on hold", CreditCardConstants.VALIDATION_ERROR);
        }

        CreditCard card = cardService.requireCard(txn.getCardId());
        assertActive(card);

        // Re-validate available credit at confirmation time (b.3)
        if (txn.getAmount().compareTo(card.getAvailableCredit()) > 0) {
            txnRepository.updateStatus(txn.getId(), CreditCardConstants.TXN_DECLINED);
            throw new CreditCardException("Insufficient available credit at confirmation",
                    CreditCardConstants.LIMIT_EXCEEDED);
        }

        applyDebit(card, txn.getAmount());
        txnRepository.updateStatus(txn.getId(), CreditCardConstants.TXN_APPROVED);
        txn.setStatus(CreditCardConstants.TXN_APPROVED);
        txn.setHoldReason(null);
        return CreditCardMapper.toResponse(txn);
    }

    @Override
    @Transactional
    public TransactionResponse payBill(String cardId, PayBillRequest request) {
        CreditCard card = cardService.requireCard(cardId);
        if (CreditCardConstants.STATUS_BLOCKED.equals(card.getStatus())) {
            throw new CreditCardException("Card is blocked", CreditCardConstants.CARD_BLOCKED);
        }

        // Route the payment through the gateway; a failure means we do not credit.
        PaymentGatewayClient.PaymentResult result =
                paymentGatewayClient.processPayment(cardId, request.getAmount(), request.getSourceReference());
        if (!result.isSuccess()) {
            throw new CreditCardException("Bill payment failed: " + result.getMessage(),
                    CreditCardConstants.VALIDATION_ERROR);
        }

        BigDecimal payment = request.getAmount();
        BigDecimal newOutstanding = card.getOutstandingAmount().subtract(payment).max(BigDecimal.ZERO);
        // Free up the credit that was actually paid down.
        BigDecimal paidDown = card.getOutstandingAmount().subtract(newOutstanding);
        BigDecimal newAvailable = card.getAvailableCredit().add(paidDown).min(card.getCreditLimit());
        cardRepository.updateBalances(cardId, newAvailable, newOutstanding);

        CreditCardTransaction txn = CreditCardTransaction.builder()
                .id(UUID.randomUUID().toString())
                .cardId(cardId)
                .type(CreditCardConstants.TXN_PAYMENT)
                .channel(CreditCardConstants.CHANNEL_ECOMMERCE)
                .amount(payment)
                .merchant("BILL_PAYMENT")
                .international(false)
                .status(CreditCardConstants.TXN_APPROVED)
                .build();
        txnRepository.save(txn);
        return CreditCardMapper.toResponse(txn);
    }

    @Override
    public List<TransactionResponse> listByCard(String cardId) {
        cardService.requireCard(cardId);
        return txnRepository.findByCardId(cardId).stream()
                .map(CreditCardMapper::toResponse)
                .toList();
    }

    // --- helpers ---

    private void assertActive(CreditCard card) {
        if (CreditCardConstants.STATUS_BLOCKED.equals(card.getStatus())) {
            throw new CreditCardException("Card is blocked", CreditCardConstants.CARD_BLOCKED);
        }
        if (!CreditCardConstants.STATUS_ACTIVE.equals(card.getStatus())) {
            throw new CreditCardException("Card is not active", CreditCardConstants.CARD_NOT_ACTIVE);
        }
    }

    /**
     * Returns a non-null hold reason when the transaction must be held:
     * b.6 amount above the high-value threshold, or b.7 too many transactions
     * within the velocity window.
     */
    private String resolveHoldReason(String cardId, BigDecimal amount) {
        if (amount.compareTo(highValueThreshold) > 0) {
            return "HIGH_VALUE: amount above " + highValueThreshold + " requires confirmation";
        }
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(velocityWindowMinutes);
        int recent = txnRepository.countSince(cardId, windowStart);
        if (recent >= velocityCount) {
            return "VELOCITY: " + recent + " transactions within " + velocityWindowMinutes + " minutes";
        }
        return null;
    }

    private void applyDebit(CreditCard card, BigDecimal amount) {
        BigDecimal newAvailable = card.getAvailableCredit().subtract(amount);
        BigDecimal newOutstanding = card.getOutstandingAmount().add(amount);
        cardRepository.updateBalances(card.getId(), newAvailable, newOutstanding);
    }
}
