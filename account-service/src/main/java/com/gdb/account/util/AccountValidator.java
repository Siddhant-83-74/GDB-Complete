package com.gdb.account.util;

import com.gdb.account.dto.request.SavingsAccountRequest;
import com.gdb.account.exception.AccountException;
import org.springframework.stereotype.Component;

// TODO: MOD1-BUG-01: Fix NullPointerException on startup/runtime validation.
// Trainee Note: This class needs to be registered as a Spring bean so that it can be injected.
// Hint: Which annotation is missing here?
@Component
public class AccountValidator {
    public void validateSavingsOnboarding(SavingsAccountRequest request) {
        if (request.getAadharNumber() == null || request.getAadharNumber().isBlank()) {
            throw new AccountException("Aadhar number is required for Savings Account onboarding", "INVALID_AADHAR");
        }
    }
}
