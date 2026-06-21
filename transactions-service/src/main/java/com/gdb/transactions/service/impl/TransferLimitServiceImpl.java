package com.gdb.transactions.service.impl;

import com.gdb.transactions.client.AccountServiceClient;
import com.gdb.transactions.domain.enums.PrivilegeLevel;
import com.gdb.transactions.domain.model.TransferLimit;
import com.gdb.transactions.dto.request.LimitCheckRequest;
import com.gdb.transactions.dto.response.TransferLimitResponse;
import com.gdb.transactions.exception.TransactionException;
import com.gdb.transactions.repository.FundTransferRepository;
import com.gdb.transactions.repository.TransferLimitRepository;
import com.gdb.transactions.service.TransferLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of TransferLimitService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferLimitServiceImpl implements TransferLimitService {

    private final TransferLimitRepository transferLimitRepository;
    private final FundTransferRepository fundTransferRepository;
    private final AccountServiceClient accountServiceClient;
    private final com.gdb.transactions.config.TransferProperties transferProperties;

    @Override
    public TransferLimitResponse getTransferLimit(Long accountNumber) {
        log.debug("Getting transfer limit for account: {}", accountNumber);

        // Get account privilege
        PrivilegeLevel privilege = accountServiceClient.getAccountPrivilege(accountNumber);
        
        // Get transfer limit rules
        TransferLimit transferLimit = transferLimitRepository.findByPrivilege(privilege)
                .orElseThrow(() -> new TransactionException("TRANSFER_LIMIT_NOT_FOUND", 
                        "Transfer limit not found for privilege: " + privilege));

        // Get daily usage
        LocalDate today = LocalDate.now();
        BigDecimal dailyUsed = fundTransferRepository.getDailyTransferAmount(accountNumber, today);
        Integer transactionsToday = fundTransferRepository.getDailyTransferCount(accountNumber, today);

        // Calculate remaining limits
        BigDecimal dailyRemaining = transferLimit.getDailyLimit().subtract(dailyUsed);
        Integer transactionsRemaining = transferLimit.getTransactionLimit() - transactionsToday;

        return TransferLimitResponse.builder()
                .accountNumber(accountNumber)
                .privilege(privilege)
                .dailyLimit(transferLimit.getDailyLimit())
                .dailyUsed(dailyUsed)
                .dailyRemaining(dailyRemaining.max(BigDecimal.ZERO))
                .transactionLimit(transferLimit.getTransactionLimit())
                .transactionsToday(transactionsToday)
                .transactionsRemaining(Math.max(0, transactionsRemaining))
                .build();
    }

    @Override
    public TransferLimitResponse getRemainingLimit(Long accountNumber) {
        TransferLimitResponse limitResponse = getTransferLimit(accountNumber);
        
        return TransferLimitResponse.builder()
                .accountNumber(accountNumber)
                .dailyRemaining(limitResponse.getDailyRemaining())
                .transactionsRemaining(limitResponse.getTransactionsRemaining())
                .build();
    }

    @Override
    public List<TransferLimit> getAllTransferRules() {
        return transferLimitRepository.findAll();
    }

    @Override
    public boolean checkTransferFeasibility(LimitCheckRequest request) {
        try {
            validateTransferLimits(request.getAccountNumber(), request.getAmount());
            return true;
        } catch (TransactionException e) {
            log.debug("Transfer not feasible for account {}: {}", request.getAccountNumber(), e.getMessage());
            return false;
        }
    }

    @Override
    public void validateTransferLimits(Long accountNumber, BigDecimal amount) {
        log.debug("Validating transfer limits for account: {}, amount: {}", accountNumber, amount);

        // Get account privilege
        PrivilegeLevel privilege = accountServiceClient.getAccountPrivilege(accountNumber);
        
        // Get transfer limit rules
        TransferLimit transferLimit = transferLimitRepository.findByPrivilege(privilege)
                .orElseThrow(() -> new TransactionException("TRANSFER_LIMIT_NOT_FOUND", 
                        "Transfer limit not found for privilege: " + privilege));

        // Check per-transaction limit
        if (amount.compareTo(transferLimit.getPerTransactionLimit()) > 0) {
            throw new TransactionException("TRANSFER_LIMIT_EXCEEDED", 
                    "Amount exceeds per-transaction limit of " + transferLimit.getPerTransactionLimit());
        }

        // TODO: MOD5-BUG-01: Limits check using TransferProperties.
        // Injected Bug: We validate if daily transfer amount exceeds a global max threshold loaded via configuration.
        // Since TransferProperties properties map incorrectly (dailyMaxLimit is 0), any transfer amount will trigger this.
        if (transferProperties != null) {
            BigDecimal globalMax = transferProperties.getDailyMax();
            if (amount.compareTo(globalMax) > 0) {
                throw new TransactionException("TRANSFER_LIMIT_EXCEEDED", 
                        "Amount exceeds global daily max limit config of " + globalMax);
            }
        }

        // Get daily usage
        LocalDate today = LocalDate.now();
        BigDecimal dailyUsed = fundTransferRepository.getDailyTransferAmount(accountNumber, today);
        Integer transactionsToday = fundTransferRepository.getDailyTransferCount(accountNumber, today);

        // Check daily amount limit
        BigDecimal newDailyTotal = dailyUsed.add(amount);
        if (newDailyTotal.compareTo(transferLimit.getDailyLimit()) > 0) {
            BigDecimal remaining = transferLimit.getDailyLimit().subtract(dailyUsed);
            throw new TransactionException("TRANSFER_LIMIT_EXCEEDED", 
                    "Transfer would exceed daily limit. Remaining limit: " + remaining);
        }

        // Check daily transaction count limit
        if (transactionsToday >= transferLimit.getTransactionLimit()) {
            throw new TransactionException("DAILY_TRANSACTION_COUNT_EXCEEDED", 
                    "Daily transaction count limit exceeded. Limit: " + transferLimit.getTransactionLimit());
        }

        log.debug("Transfer limits validated successfully for account: {}", accountNumber);
    }
}