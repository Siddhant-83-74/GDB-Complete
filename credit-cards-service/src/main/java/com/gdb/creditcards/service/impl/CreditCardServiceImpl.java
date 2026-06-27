package com.gdb.creditcards.service.impl;

import com.gdb.creditcards.client.AccountClient;
import com.gdb.creditcards.constants.CreditCardConstants;
import com.gdb.creditcards.domain.model.CardServiceLimit;
import com.gdb.creditcards.domain.model.CreditCard;
import com.gdb.creditcards.dto.request.ApplyCardRequest;
import com.gdb.creditcards.dto.request.SetServiceLimitRequest;
import com.gdb.creditcards.dto.response.CardOperationResponse;
import com.gdb.creditcards.dto.response.CreditCardResponse;
import com.gdb.creditcards.exception.CreditCardException;
import com.gdb.creditcards.factory.CardLimitPolicy;
import com.gdb.creditcards.mapper.CreditCardMapper;
import com.gdb.creditcards.repository.CreditCardRepository;
import com.gdb.creditcards.service.CreditCardService;
import com.gdb.creditcards.util.CardNumberGenerator;
import com.gdb.creditcards.util.CardValidator;
import com.gdb.creditcards.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreditCardServiceImpl implements CreditCardService {

    private final CreditCardRepository cardRepository;
    private final CryptoUtil cryptoUtil;
    private final AccountClient accountClient;

    @Value("${app.rules.min-expiry-years:3}")
    private int minExpiryYears;

    @Override
    public CreditCardResponse apply(ApplyCardRequest request) {
        // Business-rule validation (b.2, b.4, b.5, b.8, b.10)
        CardValidator.validateName(request.getCardHolderName());
        CardValidator.validateMobile(request.getMobileNumber());
        CardValidator.validateVendor(request.getVendor());
        CardValidator.validateCvv(request.getCvv());
        CardValidator.validateExpiry(request.getExpiryDate(), minExpiryYears, LocalDate.now());

        // b.10 / b.11: generate a 16-digit PAN with the vendor's prefix + Luhn check
        String pan = CardNumberGenerator.generate(request.getVendor());
        CardValidator.validateCardNumber(pan); // b.1 defensive re-check

        // b.13: category-based credit limit
        java.math.BigDecimal creditLimit = CardLimitPolicy.creditLimitFor(request.getCategory());

        CreditCard card = CreditCard.builder()
                .id(UUID.randomUUID().toString())
                .userId(request.getUserId())
                .cardHolderName(request.getCardHolderName())
                .mobileNumber(request.getMobileNumber())
                // Security (f): PAN encrypted at rest; CVV stored only as a bcrypt hash
                .cardNumberEncrypted(cryptoUtil.encrypt(pan))
                .cardBin(pan.substring(0, 6))
                .cardLast4(pan.substring(pan.length() - 4))
                .vendor(request.getVendor())
                .category(request.getCategory())
                .cvvHash(BCrypt.hashpw(request.getCvv(), BCrypt.gensalt()))
                .expiryDate(request.getExpiryDate())
                .creditLimit(creditLimit)
                .availableCredit(creditLimit)
                .outstandingAmount(java.math.BigDecimal.ZERO)
                .internationalEnabled(false) // b.16 off by default
                .status(CreditCardConstants.STATUS_ACTIVE)
                // Admin application metadata
                .consentSource(request.getConsentSources() == null ? null
                        : String.join(",", request.getConsentSources()))
                .otpVerified(Boolean.TRUE.equals(request.getOtpVerified()))
                .leadSource(request.getLeadSource())
                .sourcingBranchCode(request.getSourcingBranchCode())
                .kycDocumentName(request.getKycDocumentName())
                .incomeDocumentName(request.getIncomeDocumentName())
                .build();

        // b.14: optional account mapping at creation, verified via Account Service
        if (request.getLinkedAccountNumber() != null) {
            if (!accountClient.accountExists(request.getLinkedAccountNumber())) {
                throw new CreditCardException("Linked account not found or unverifiable",
                        CreditCardConstants.VALIDATION_ERROR);
            }
            card.setLinkedAccountNumber(request.getLinkedAccountNumber());
        }

        cardRepository.save(card);
        log.info("Issued {} {} card ending {} for user {}",
                card.getCategory(), card.getVendor(), card.getCardLast4(), card.getUserId());
        return CreditCardMapper.toResponse(card, List.of());
    }

    @Override
    public List<CreditCardResponse> listByUser(Long userId) {
        return cardRepository.findByUserId(userId).stream()
                .map(c -> CreditCardMapper.toResponse(c, cardRepository.findServiceLimits(c.getId())))
                .toList();
    }

    @Override
    public List<CreditCardResponse> listAll() {
        return cardRepository.findAll().stream()
                .map(c -> CreditCardMapper.toResponse(c, cardRepository.findServiceLimits(c.getId())))
                .toList();
    }

    @Override
    public CreditCardResponse getById(String id) {
        CreditCard card = requireCard(id);
        return CreditCardMapper.toResponse(card, cardRepository.findServiceLimits(id));
    }

    @Override
    public CardOperationResponse updateStatus(String id, String status) {
        requireCard(id);
        cardRepository.updateStatus(id, status);
        return CardOperationResponse.builder()
                .success(true)
                .status(status)
                .message("Card status updated to " + status)
                .build();
    }

    @Override
    public CardOperationResponse setServiceLimit(String id, SetServiceLimitRequest request) {
        requireCard(id);
        CardServiceLimit limit = cardRepository.findServiceLimit(id, request.getChannel())
                .orElseGet(() -> CardServiceLimit.builder()
                        .id(UUID.randomUUID().toString())
                        .cardId(id)
                        .channel(request.getChannel())
                        .build());
        limit.setPerTxnLimit(request.getPerTxnLimit());
        cardRepository.upsertServiceLimit(limit);
        return CardOperationResponse.builder()
                .success(true)
                .message(request.getChannel() + " per-transaction limit set to " + request.getPerTxnLimit())
                .build();
    }

    @Override
    public CardOperationResponse toggleInternational(String id, boolean enabled) {
        requireCard(id);
        cardRepository.updateInternational(id, enabled);
        return CardOperationResponse.builder()
                .success(true)
                .message("International transactions " + (enabled ? "enabled" : "disabled"))
                .build();
    }

    @Override
    public CardOperationResponse mapAccount(String id, Long accountNumber) {
        requireCard(id);
        if (!accountClient.accountExists(accountNumber)) {
            throw new CreditCardException("Account not found or unverifiable",
                    CreditCardConstants.VALIDATION_ERROR);
        }
        cardRepository.linkAccount(id, accountNumber);
        return CardOperationResponse.builder()
                .success(true)
                .message("Card linked to account " + accountNumber)
                .build();
    }

    @Override
    public CreditCard requireCard(String id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new CreditCardException("Credit card not found: " + id,
                        CreditCardConstants.CARD_NOT_FOUND));
    }
}
