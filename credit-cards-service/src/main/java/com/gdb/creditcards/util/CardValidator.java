package com.gdb.creditcards.util;

import com.gdb.creditcards.constants.CreditCardConstants;
import com.gdb.creditcards.exception.CreditCardException;

import java.time.LocalDate;

/**
 * Centralizes the structural / temporal business-rule checks that Bean
 * Validation cannot express on its own.
 */
public final class CardValidator {

    private CardValidator() {
    }

    /** b.1 16 numeric digits only. */
    public static void validateCardNumber(String pan) {
        if (pan == null || !pan.matches("\\d{16}")) {
            throw new CreditCardException("Card number must be exactly 16 digits",
                    CreditCardConstants.VALIDATION_ERROR);
        }
    }

    /** b.4 CVV is exactly 3 digits. */
    public static void validateCvv(String cvv) {
        if (cvv == null || !cvv.matches("\\d{3}")) {
            throw new CreditCardException("CVV must be exactly 3 digits",
                    CreditCardConstants.VALIDATION_ERROR);
        }
    }

    /** b.5 Mobile number must be present and 10 digits. */
    public static void validateMobile(String mobile) {
        if (mobile == null || !mobile.matches("\\d{10}")) {
            throw new CreditCardException("A valid 10-digit mobile number must be mapped to the card",
                    CreditCardConstants.VALIDATION_ERROR);
        }
    }

    /** b.8 Card holder name is mandatory. */
    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new CreditCardException("Card holder name is mandatory",
                    CreditCardConstants.VALIDATION_ERROR);
        }
    }

    /** b.2 Expiry date cannot be less than {@code minYears} from today. */
    public static void validateExpiry(LocalDate expiry, int minYears, LocalDate today) {
        if (expiry == null) {
            throw new CreditCardException("Expiry date is required", CreditCardConstants.VALIDATION_ERROR);
        }
        LocalDate earliestAllowed = today.plusYears(minYears);
        if (expiry.isBefore(earliestAllowed)) {
            throw new CreditCardException(
                    "Expiry date must be at least " + minYears + " years from today",
                    CreditCardConstants.VALIDATION_ERROR);
        }
    }

    /** b.10 Vendor must be one of the supported networks. */
    public static void validateVendor(String vendor) {
        if (vendor == null
                || !(vendor.equals(CreditCardConstants.VENDOR_VISA)
                        || vendor.equals(CreditCardConstants.VENDOR_MASTERCARD)
                        || vendor.equals(CreditCardConstants.VENDOR_RUPAY))) {
            throw new CreditCardException("Card type must be VISA, MASTERCARD or RUPAY",
                    CreditCardConstants.INVALID_CARD_TYPE);
        }
    }
}
