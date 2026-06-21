package com.gdb.transactions.service;

import com.gdb.transactions.dto.request.WithdrawRequest;
import com.gdb.transactions.dto.response.TransactionResponse;

/**
 * Service interface for withdrawal operations.
 */
public interface WithdrawService {
    
    /**
     * Process a withdrawal request.
     */
    TransactionResponse processWithdraw(WithdrawRequest request);
}