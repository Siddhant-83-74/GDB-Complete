package com.gdb.transactions.service;

import com.gdb.transactions.dto.request.FundTransferRequest;
import com.gdb.transactions.dto.response.FundTransferResponse;

/**
 * Service interface for fund transfer operations.
 */
public interface TransferService {
    
    /**
     * Process a fund transfer request.
     */
    FundTransferResponse processTransfer(FundTransferRequest request);
}