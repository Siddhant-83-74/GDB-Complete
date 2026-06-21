package com.gdb.transactions.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for account operations (debit/credit).
 * Must match the account service's AccountOperationResponse structure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountOperationResponse {
    private String status;
    private Long accountNumber;
    private BigDecimal amount;
    private BigDecimal previousBalance;
    private BigDecimal newBalance;
    private String description;
    private String operationType; // DEBIT or CREDIT
    private LocalDateTime timestamp;
    private String idempotencyKey;
}