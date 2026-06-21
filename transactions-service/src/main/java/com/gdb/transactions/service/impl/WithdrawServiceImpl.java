package com.gdb.transactions.service.impl;

import com.gdb.transactions.client.AccountServiceClient;
import com.gdb.transactions.client.dto.AccountOperationResponse;
import com.gdb.transactions.client.dto.AccountResponse;
import com.gdb.transactions.domain.enums.TransactionType;
import com.gdb.transactions.domain.model.TransactionLog;
import com.gdb.transactions.dto.request.WithdrawRequest;
import com.gdb.transactions.dto.response.TransactionResponse;
import com.gdb.transactions.exception.TransactionException;
import com.gdb.transactions.service.TransactionLogService;
import com.gdb.transactions.service.WithdrawService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of WithdrawService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawServiceImpl implements WithdrawService {

    private final AccountServiceClient accountServiceClient;
    private final TransactionLogService transactionLogService;

    @Override
    @Transactional
    public TransactionResponse processWithdraw(WithdrawRequest request) {
        log.info("Processing withdrawal for account: {}, amount: {}", request.getAccountNumber(), request.getAmount());

        try {
            // 1. Validate account exists and is active
            AccountResponse account = accountServiceClient.validateAccount(request.getAccountNumber());
            if (!account.getIsActive()) {
                throw new TransactionException("ACCOUNT_NOT_ACTIVE", "Account is not active");
            }

            // 2. Verify PIN
            boolean pinValid = accountServiceClient.verifyPin(request.getAccountNumber(), request.getPin());
            if (!pinValid) {
                throw new TransactionException("INVALID_PIN", "Invalid PIN provided");
            }

            // 3. Check sufficient balance and debit account
            AccountOperationResponse debitResponse = accountServiceClient.debitAccount(
                    request.getAccountNumber(),
                    request.getAmount(),
                    "Withdrawal: " + (request.getDescription() != null ? request.getDescription() : "Cash withdrawal"));

            // 4. Log transaction
            TransactionLog transactionLog = TransactionLog.builder()
                    .accountNumber(request.getAccountNumber())
                    .amount(request.getAmount())
                    .transactionType(TransactionType.WITHDRAW)
                    .description(request.getDescription() != null ? request.getDescription() : "Cash withdrawal")
                    .status("SUCCESS")
                    .build();

            TransactionLog savedLog = transactionLogService.logTransaction(transactionLog);

            // 5. Return success response
            TransactionResponse response = TransactionResponse.builder()
                    .status("SUCCESS")
                    .transactionId(savedLog.getId())
                    .accountNumber(request.getAccountNumber())
                    .amount(request.getAmount())
                    .transactionType(TransactionType.WITHDRAW)
                    .description(transactionLog.getDescription())
                    .newBalance(debitResponse.getNewBalance())
                    .transactionDate(savedLog.getCreatedAt() != null ? savedLog.getCreatedAt() : LocalDateTime.now())
                    .build();

            log.info("Withdrawal processed successfully for account: {}, transaction ID: {}",
                    request.getAccountNumber(), savedLog.getId());

            return response;

        } catch (TransactionException e) {
            log.error("Withdrawal failed for account: {} - {}", request.getAccountNumber(), e.getMessage());

            // Log failed transaction
            try {
                TransactionLog failedLog = TransactionLog.builder()
                        .accountNumber(request.getAccountNumber())
                        .amount(request.getAmount())
                        .transactionType(TransactionType.WITHDRAW)
                        .description("Failed: " + e.getMessage())
                        .status("FAILED")
                        .build();
                transactionLogService.logTransaction(failedLog);
            } catch (Exception logEx) {
                log.error("Failed to log failed transaction: {}", logEx.getMessage());
            }
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during withdrawal for account: {}", request.getAccountNumber(), e);
            throw new TransactionException("WITHDRAWAL_FAILED", "Withdrawal operation failed");
        }
    }
}