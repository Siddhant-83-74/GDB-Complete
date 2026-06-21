package com.gdb.transactions.repository;

import com.gdb.transactions.domain.model.FundTransfer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for fund transfer operations.
 */
public interface FundTransferRepository {
    
    /**
     * Save a fund transfer record.
     */
    FundTransfer save(FundTransfer fundTransfer);
    
    /**
     * Find fund transfer by ID.
     */
    Optional<FundTransfer> findById(Long id);
    
    /**
     * Get daily transfer amount for an account.
     */
    BigDecimal getDailyTransferAmount(Long accountNumber, LocalDate date);
    
    /**
     * Get daily transfer count for an account.
     */
    Integer getDailyTransferCount(Long accountNumber, LocalDate date);
    
    /**
     * Find transfers by account (either from or to).
     */
    List<FundTransfer> findByAccount(Long accountNumber, int limit, int offset);
    
    /**
     * Find all transfers with pagination.
     */
    List<FundTransfer> findAll(int limit, int offset);
    
    /**
     * Count total transfers.
     */
    Long countAll();
}