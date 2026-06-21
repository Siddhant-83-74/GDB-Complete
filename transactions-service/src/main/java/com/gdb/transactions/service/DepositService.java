package com.gdb.transactions.service;

import com.gdb.transactions.dto.request.DepositRequest;
import com.gdb.transactions.dto.response.TransactionResponse;

/**
 * Service interface for deposit operations.
 */
public interface DepositService {
    
    /**
     * Process a deposit request.
     */
    TransactionResponse processDeposit(DepositRequest request);
}