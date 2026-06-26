package com.gdb.creditcards.factory;

import com.gdb.creditcards.constants.CreditCardConstants;
import com.gdb.creditcards.exception.CreditCardException;

import java.math.BigDecimal;

/**
 * b.13 Resolves the default credit limit for a card category/tier.
 * Centralizing this keeps the "limits for the category of the card" rule in
 * one place so it can be tuned independently of card creation logic.
 */
public final class CardLimitPolicy {

    private CardLimitPolicy() {
    }

    public static BigDecimal creditLimitFor(String category) {
        if (category == null) {
            throw new CreditCardException("Card category is required", CreditCardConstants.INVALID_CARD_TYPE);
        }
        return switch (category.toUpperCase()) {
            case CreditCardConstants.CATEGORY_SILVER -> new BigDecimal("100000");
            case CreditCardConstants.CATEGORY_GOLD -> new BigDecimal("300000");
            case CreditCardConstants.CATEGORY_PLATINUM -> new BigDecimal("1000000");
            default -> throw new CreditCardException("Unknown card category: " + category,
                    CreditCardConstants.INVALID_CARD_TYPE);
        };
    }
}
