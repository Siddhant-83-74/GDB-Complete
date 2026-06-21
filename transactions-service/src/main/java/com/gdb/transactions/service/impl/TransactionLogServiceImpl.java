package com.gdb.transactions.service.impl;

import com.gdb.transactions.domain.enums.TransactionType;
import com.gdb.transactions.domain.model.TransactionLog;
import com.gdb.transactions.dto.response.TransactionLogResponse;
import com.gdb.transactions.repository.TransactionLogRepository;
import com.gdb.transactions.service.TransactionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    @Override
    public TransactionLog logTransaction(TransactionLog transactionLog) {
        log.debug("Logging transaction for account: {}, type: {}, amount: {}", 
                transactionLog.getAccountNumber(), transactionLog.getTransactionType(), transactionLog.getAmount());
        
        return transactionLogRepository.save(transactionLog);
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
                .createdAt(log.getCreatedAt())
                .build();
    }
}