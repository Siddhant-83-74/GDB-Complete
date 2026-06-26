package com.gdb.creditcards.mapper;

import com.gdb.creditcards.domain.model.CardServiceLimit;
import com.gdb.creditcards.domain.model.CreditCard;
import com.gdb.creditcards.domain.model.CreditCardTransaction;
import com.gdb.creditcards.dto.response.CreditCardResponse;
import com.gdb.creditcards.dto.response.TransactionResponse;
import com.gdb.creditcards.util.MaskingUtil;

import java.util.List;

/**
 * Maps domain models to API responses. All PAN exposure goes through
 * {@link MaskingUtil} so the full card number never leaves the service (b.12).
 */
public final class CreditCardMapper {

    private CreditCardMapper() {
    }

    public static CreditCardResponse toResponse(CreditCard card, List<CardServiceLimit> limits) {
        List<CreditCardResponse.ServiceLimitView> limitViews = limits == null ? List.of()
                : limits.stream()
                        .map(l -> CreditCardResponse.ServiceLimitView.builder()
                                .channel(l.getChannel())
                                .perTxnLimit(l.getPerTxnLimit())
                                .build())
                        .toList();

        return CreditCardResponse.builder()
                .id(card.getId())
                .userId(card.getUserId())
                .cardHolderName(card.getCardHolderName())
                .maskedCardNumber(MaskingUtil.maskFromLast4(card.getCardLast4()))
                .vendor(card.getVendor())
                .category(card.getCategory())
                .expiryDate(card.getExpiryDate())
                .mobileNumber(card.getMobileNumber())
                .creditLimit(card.getCreditLimit())
                .availableCredit(card.getAvailableCredit())
                .outstandingAmount(card.getOutstandingAmount())
                .linkedAccountNumber(card.getLinkedAccountNumber())
                .internationalEnabled(card.getInternationalEnabled())
                .status(card.getStatus())
                .serviceLimits(limitViews)
                .build();
    }

    public static TransactionResponse toResponse(CreditCardTransaction txn) {
        return TransactionResponse.builder()
                .id(txn.getId())
                .cardId(txn.getCardId())
                .type(txn.getType())
                .channel(txn.getChannel())
                .amount(txn.getAmount())
                .merchant(txn.getMerchant())
                .international(txn.isInternational())
                .status(txn.getStatus())
                .holdReason(txn.getHoldReason())
                .createdAt(txn.getCreatedAt())
                .build();
    }
}
