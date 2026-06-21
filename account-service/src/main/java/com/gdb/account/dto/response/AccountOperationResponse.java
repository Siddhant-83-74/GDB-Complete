package com.gdb.account.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for internal account operations (debit/credit).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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