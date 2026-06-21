package com.gdb.transactions.service;

import com.gdb.transactions.dto.request.LimitCheckRequest;
import com.gdb.transactions.dto.response.TransferLimitResponse;
import com.gdb.transactions.domain.model.TransferLimit;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for transfer limit operations.
 */
public interface TransferLimitService {
    
    /**
     * Get transfer limit information for an account.
     */
    TransferLimitResponse getTransferLimit(Long accountNumber);
    
    /**
     * Get remaining transfer limit for an account.
     */
    TransferLimitResponse getRemainingLimit(Long accountNumber);
    
    /**
     * Get all transfer rules.
     */
    List<TransferLimit> getAllTransferRules();
    
    /**
     * Check if transfer is feasible within limits.
     */
    boolean checkTransferFeasibility(LimitCheckRequest request);
    
    /**
     * Validate transfer against limits.
     */
    void validateTransferLimits(Long accountNumber, BigDecimal amount);
}