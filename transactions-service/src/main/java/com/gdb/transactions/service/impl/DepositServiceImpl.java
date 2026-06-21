package com.gdb.transactions.service.impl;

import com.gdb.transactions.client.AccountServiceClient;
import com.gdb.transactions.client.dto.AccountOperationResponse;
import com.gdb.transactions.client.dto.AccountResponse;
import com.gdb.transactions.domain.enums.TransactionType;
import com.gdb.transactions.domain.model.TransactionLog;
import com.gdb.transactions.dto.request.DepositRequest;
import com.gdb.transactions.dto.response.TransactionResponse;
import com.gdb.transactions.exception.TransactionException;
import com.gdb.transactions.service.DepositService;
import com.gdb.transactions.service.TransactionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of DepositService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepositServiceImpl implements DepositService {

    private final AccountServiceClient accountServiceClient;
    private final TransactionLogService transactionLogService;

    @Override
    @Transactional
    public TransactionResponse processDeposit(DepositRequest request) {
        log.info("Processing deposit for account: {}, amount: {}", request.getAccountNumber(), request.getAmount());

        try {
            // 1. Validate account exists and is active
            AccountResponse account = accountServiceClient.validateAccount(request.getAccountNumber());
            if (!account.getIsActive()) {
                throw new TransactionException("ACCOUNT_NOT_ACTIVE", "Account is not active");
            }

            // 2. Credit account
            AccountOperationResponse creditResponse = accountServiceClient.creditAccount(
                    request.getAccountNumber(),
                    request.getAmount(),
                    "Deposit: " + (request.getDescription() != null ? request.getDescription() : "Cash deposit"));

            // 3. Log transaction
            TransactionLog transactionLog = TransactionLog.builder()
                    .accountNumber(request.getAccountNumber())
                    .amount(request.getAmount())
                    .transactionType(TransactionType.DEPOSIT)
                    .description(request.getDescription() != null ? request.getDescription() : "Cash deposit")
                    .status("SUCCESS")
                    .build();

            TransactionLog savedLog = transactionLogService.logTransaction(transactionLog);

            // 4. Return success response
            TransactionResponse response = TransactionResponse.builder()
                    .status("SUCCESS")
                    .transactionId(savedLog.getId())
                    .accountNumber(request.getAccountNumber())
                    .amount(request.getAmount())
                    .transactionType(TransactionType.DEPOSIT)
                    .description(transactionLog.getDescription())
                    .newBalance(creditResponse.getNewBalance())
                    .transactionDate(savedLog.getCreatedAt() != null ? savedLog.getCreatedAt() : LocalDateTime.now())
                    .build();

            log.info("Deposit processed successfully for account: {}, transaction ID: {}",
                    request.getAccountNumber(), savedLog.getId());

            return response;

        } catch (TransactionException e) {
            log.error("Deposit failed for account: {} - {}", request.getAccountNumber(), e.getMessage());

            // Log failed transaction
            try {
                TransactionLog failedLog = TransactionLog.builder()
                        .accountNumber(request.getAccountNumber())
                        .amount(request.getAmount())
                        .transactionType(TransactionType.DEPOSIT)
                        .description("Failed: " + e.getMessage())
                        .status("FAILED")
                        .build();
                transactionLogService.logTransaction(failedLog);
            } catch (Exception logEx) {
                log.error("Failed to log failed transaction: {}", logEx.getMessage());
            }
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during deposit for account: {}", request.getAccountNumber(), e);
            throw new TransactionException("DEPOSIT_FAILED", "Deposit operation failed");
        }
    }
}