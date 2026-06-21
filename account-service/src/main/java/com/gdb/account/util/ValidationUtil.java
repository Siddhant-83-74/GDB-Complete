package com.gdb.account.util;

import com.gdb.account.exception.AccountException;
import com.gdb.account.constants.AccountConstants;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

/**
 * Utility for business rule validations.
 */
public class ValidationUtil {

    public static void validatePin(String pin) {
        if (pin == null || pin.length() != 4 || !pin.matches("\\d+")) {
            throw new AccountException("PIN must be 4 numeric digits", AccountConstants.INVALID_PIN);
        }

        // Identical digits (e.g., 1111)
        if (pin.charAt(0) == pin.charAt(1) && pin.charAt(1) == pin.charAt(2) && pin.charAt(2) == pin.charAt(3)) {
            throw new AccountException("PIN cannot have all identical digits", AccountConstants.INVALID_PIN);
        }

        // Sequential digits (e.g., 1234, 4321)
        if (isSequential(pin)) {
            throw new AccountException("PIN cannot be sequential", AccountConstants.INVALID_PIN);
        }
    }

    private static boolean isSequential(String pin) {
        int d0 = pin.charAt(0) - '0';
        int d1 = pin.charAt(1) - '0';
        int d2 = pin.charAt(2) - '0';
        int d3 = pin.charAt(3) - '0';

        return (d1 == d0 + 1 && d2 == d1 + 1 && d3 == d2 + 1) ||
                (d1 == d0 - 1 && d2 == d1 - 1 && d3 == d2 - 1);
    }

    public static void validateAge(String dob) {
        LocalDate birthDate = LocalDate.parse(dob, DateTimeFormatter.ISO_LOCAL_DATE);
        if (Period.between(birthDate, LocalDate.now()).getYears() < 18) {
            throw new AccountException("Minimum age requirement is 18 years", AccountConstants.INVALID_AGE);
        }
    }

    private ValidationUtil() {
    }
}
