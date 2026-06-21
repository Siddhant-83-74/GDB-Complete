package com.gdb.transactions.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for checking transfer limits.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LimitCheckRequest {
    
    @NotNull(message = "Account number is required")
    @Positive(message = "Account number must be positive")
    private Long accountNumber;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal amount;
}