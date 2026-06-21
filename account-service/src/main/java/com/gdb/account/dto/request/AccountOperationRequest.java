package com.gdb.account.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for internal account operations (debit/credit).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountOperationRequest {
    
    @NotNull(message = "Account number is required")
    private Long accountNumber;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private String description;
    
    private String idempotencyKey;
}