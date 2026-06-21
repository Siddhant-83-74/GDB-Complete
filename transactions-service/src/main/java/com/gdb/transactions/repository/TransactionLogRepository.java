package com.gdb.transactions.repository;

import com.gdb.transactions.domain.enums.TransactionType;
import com.gdb.transactions.domain.model.TransactionLog;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for transaction log operations.
 */
public interface TransactionLogRepository {
    
    /**
     * Save a transaction log entry.
     */
    TransactionLog save(TransactionLog transactionLog);
    
    /**
     * Find transaction log by ID.
     */
    Optional<TransactionLog> findById(Long id);
    
    /**
     * Find transaction logs by account number.
     */
    List<TransactionLog> findByAccountNumber(Long accountNumber, int limit, int offset);
    
    /**
     * Find transaction logs by account number and type.
     */
    List<TransactionLog> findByAccountNumberAndType(Long accountNumber, TransactionType type, int limit, int offset);
    
    /**
     * Find transaction logs by account number and date range.
     */
    List<TransactionLog> findByAccountNumberAndDateRange(Long accountNumber, LocalDate startDate, LocalDate endDate, int limit, int offset);
    
    /**
     * Find all transaction logs with pagination.
     */
    List<TransactionLog> findAll(int limit, int offset);
    
    /**
     * Count total transaction logs.
     */
    Long countAll();
    
    /**
     * Count transaction logs by account number.
     */
    Long countByAccountNumber(Long accountNumber);
}