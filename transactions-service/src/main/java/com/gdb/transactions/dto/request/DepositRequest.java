package com.gdb.transactions.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for deposit operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequest {

    @NotNull(message = "Account number is required")
    @Positive(message = "Account number must be positive")
    @JsonProperty("account_number")
    private Long accountNumber;

    // TODO: MOD6-CR-01: Introduce Deposit transaction cap.
    // Trainee task: Add validation constraint (e.g. @DecimalMax) to prevent single deposits 
    // exceeding $50,000.00 without additional tax/compliance identification.
    @DecimalMax(value="50000.00",message="Deposits cannot exceed 50,000.00")
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least 1.00")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal amount;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    private String pin;
}