package com.gdb.creditcards.util;

import com.gdb.creditcards.constants.CreditCardConstants;
import com.gdb.creditcards.exception.CreditCardException;

import java.security.SecureRandom;

/**
 * Generates a 16-digit PAN (b.1) whose prefix matches the requested vendor
 * (b.11) and whose final digit is a valid Luhn check digit (so the number
 * passes b.1's "16 numbers only" plus standard network validation).
 *
 * IIN ranges used (simplified, representative):
 *   VISA       -> starts with 4
 *   MASTERCARD -> starts with 51-55
 *   RUPAY      -> starts with 60/65/81
 */
public final class CardNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private CardNumberGenerator() {
    }

    public static String generate(String vendor) {
        String prefix = prefixFor(vendor);
        StringBuilder sb = new StringBuilder(prefix);
        // Fill up to 15 digits, leaving room for the Luhn check digit.
        while (sb.length() < 15) {
            sb.append(RANDOM.nextInt(10));
        }
        sb.append(luhnCheckDigit(sb.toString()));
        return sb.toString();
    }

    private static String prefixFor(String vendor) {
        if (vendor == null) {
            throw new CreditCardException("Vendor is required", CreditCardConstants.INVALID_CARD_TYPE);
        }
        return switch (vendor.toUpperCase()) {
            case CreditCardConstants.VENDOR_VISA -> "4";
            case CreditCardConstants.VENDOR_MASTERCARD -> "5" + (1 + RANDOM.nextInt(5)); // 51-55
            case CreditCardConstants.VENDOR_RUPAY -> pick("60", "65", "81");
            default -> throw new CreditCardException("Unsupported vendor: " + vendor,
                    CreditCardConstants.INVALID_CARD_TYPE);
        };
    }

    private static String pick(String... options) {
        return options[RANDOM.nextInt(options.length)];
    }

    /**
     * Computes the Luhn check digit for a partial number (without the check digit).
     */
    static int luhnCheckDigit(String partial) {
        int sum = 0;
        boolean doubleIt = true; // rightmost partial digit is doubled
        for (int i = partial.length() - 1; i >= 0; i--) {
            int d = partial.charAt(i) - '0';
            if (doubleIt) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubleIt = !doubleIt;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * Validates a full PAN against the Luhn algorithm.
     */
    public static boolean isLuhnValid(String pan) {
        if (pan == null || !pan.matches("\\d{16}")) {
            return false;
        }
        int sum = 0;
        boolean doubleIt = false;
        for (int i = pan.length() - 1; i >= 0; i--) {
            int d = pan.charAt(i) - '0';
            if (doubleIt) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }
}
