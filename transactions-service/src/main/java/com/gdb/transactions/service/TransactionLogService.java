package com.gdb.transactions.service;

import com.gdb.transactions.domain.enums.TransactionType;
import com.gdb.transactions.domain.model.TransactionLog;
import com.gdb.transactions.dto.response.TransactionLogResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for transaction logging operations.
 */
public interface TransactionLogService {
    
    /**
     * Log a transaction.
     */
    TransactionLog logTransaction(TransactionLog transactionLog);
    
    /**
     * Get all transaction logs with pagination.
     */
    List<TransactionLogResponse> getAllTransactionLogs(int limit, int offset);
    
    /**
     * Get transaction logs for a specific account.
     */
    List<TransactionLogResponse> getAccountTransactionLogs(Long accountNumber, int limit, int offset);
    
    /**
     * Get transaction logs for a specific account and type.
     */
    List<TransactionLogResponse> getAccountTransactionLogsByType(Long accountNumber, TransactionType type, int limit, int offset);
    
    /**
     * Get transaction logs for a specific account and date range.
     */
    List<TransactionLogResponse> getAccountTransactionLogsByDateRange(Long accountNumber, LocalDate startDate, LocalDate endDate, int limit, int offset);
    
    /**
     * Get total count of all transaction logs.
     */
    Long getTotalCount();
    
    /**
     * Get total count of transaction logs for an account.
     */
    Long getAccountTransactionCount(Long accountNumber);
}