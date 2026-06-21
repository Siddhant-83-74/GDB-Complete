package com.gdb.transactions.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for account operations (debit/credit).
 * Must match the account service's AccountOperationRequest structure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountOperationRequest {
    private Long accountNumber;
    private BigDecimal amount;
    private String description;
    private String idempotencyKey;
}