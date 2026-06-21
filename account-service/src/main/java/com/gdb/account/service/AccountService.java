package com.gdb.account.service;

import com.gdb.account.dto.request.AccountOperationRequest;
import com.gdb.account.dto.request.SavingsAccountRequest;
import com.gdb.account.dto.request.CurrentAccountRequest;
import com.gdb.account.dto.response.AccountOperationResponse;
import com.gdb.account.dto.response.AccountResponse;

import java.util.List;

/**
 * Service interface for Account operations.
 */
public interface AccountService {

    AccountResponse createSavingsAccount(SavingsAccountRequest request);

    AccountResponse createCurrentAccount(CurrentAccountRequest request);

    AccountResponse getAccountByNumber(Long accountNumber);

    List<AccountResponse> getAllAccounts(String type, String privilege, Boolean isActive);

    // Internal API methods for service-to-service communication
    AccountOperationResponse debitAccount(AccountOperationRequest request);

    AccountOperationResponse creditAccount(AccountOperationRequest request);

    boolean isAccountActive(Long accountNumber);

    String getAccountPrivilege(Long accountNumber);

    boolean verifyPin(Long accountNumber, String pin);
}
