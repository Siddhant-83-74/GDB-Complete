package com.gdb.transactions.service.impl;

import com.gdb.transactions.domain.enums.TransactionType;
import com.gdb.transactions.domain.model.TransactionLog;
import com.gdb.transactions.dto.response.TransactionLogResponse;
import com.gdb.transactions.repository.TransactionLogRepository;
import com.gdb.transactions.service.TransactionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of TransactionLogService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionLogServiceImpl implements TransactionLogService {

    private final TransactionLogRepository transactionLogRepository;

    /** Transactions at or above this amount are auto-flagged as suspicious. */
    private static final BigDecimal SUSPICION_AMOUNT_THRESHOLD = new BigDecimal("50000");

    @Override
    public TransactionLog logTransaction(TransactionLog transactionLog) {
        log.debug("Logging transaction for account: {}, type: {}, amount: {}",
                transactionLog.getAccountNumber(), transactionLog.getTransactionType(), transactionLog.getAmount());

        // Auto-detect suspicious activity unless the caller already set the flag explicitly.
        if (transactionLog.getSuspicious() == null) {
            transactionLog.setSuspicious(isSuspicious(transactionLog));
        }
        if (Boolean.TRUE.equals(transactionLog.getSuspicious())) {
            log.warn("Suspicious transaction detected for account: {}, amount: {}",
                    transactionLog.getAccountNumber(), transactionLog.getAmount());
        }

        return transactionLogRepository.save(transactionLog);
    }

    /**
     * Heuristic rules for flagging a transaction as suspicious.
     * Currently flags high-value transactions and failed transactions.
     */
    private boolean isSuspicious(TransactionLog transactionLog) {
        BigDecimal amount = transactionLog.getAmount();
        if (amount != null && amount.compareTo(SUSPICION_AMOUNT_THRESHOLD) >= 0) {
            return true;
        }
        return "FAILED".equalsIgnoreCase(transactionLog.getStatus());
    }

    @Override
    public List<TransactionLogResponse> getAllTransactionLogs(int limit, int offset) {
        List<TransactionLog> logs = transactionLogRepository.findAll(limit, offset);
        return logs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionLogResponse> getAccountTransactionLogs(Long accountNumber, int limit, int offset) {
        List<TransactionLog> logs = transactionLogRepository.findByAccountNumber(accountNumber, limit, offset);
        return logs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionLogResponse> getAccountTransactionLogsByType(Long accountNumber, TransactionType type, int limit, int offset) {
        List<TransactionLog> logs = transactionLogRepository.findByAccountNumberAndType(accountNumber, type, limit, offset);
        return logs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionLogResponse> getAccountTransactionLogsByDateRange(Long accountNumber, LocalDate startDate, LocalDate endDate, int limit, int offset) {
        List<TransactionLog> logs = transactionLogRepository.findByAccountNumberAndDateRange(accountNumber, startDate, endDate, limit, offset);
        return logs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionLogResponse> getSuspiciousTransactions(int limit, int offset) {
        List<TransactionLog> logs = transactionLogRepository.findSuspicious(limit, offset);
        return logs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean flagTransaction(Long id, boolean suspicious) {
        log.info("Manually setting suspicious={} for transaction: {}", suspicious, id);
        return transactionLogRepository.updateSuspiciousFlag(id, suspicious) > 0;
    }

    @Override
    public Long getSuspiciousCount() {
        return transactionLogRepository.countSuspicious();
    }

    @Override
    public Long getTotalCount() {
        return transactionLogRepository.countAll();
    }

    @Override
    public Long getAccountTransactionCount(Long accountNumber) {
        return transactionLogRepository.countByAccountNumber(accountNumber);
    }

    private TransactionLogResponse mapToResponse(TransactionLog log) {
        return TransactionLogResponse.builder()
                .id(log.getId())
                .accountNumber(log.getAccountNumber())
                .amount(log.getAmount())
                .transactionType(log.getTransactionType())
                .suspicious(log.getSuspicious())
                .createdAt(log.getCreatedAt())
                .build();
    }
}