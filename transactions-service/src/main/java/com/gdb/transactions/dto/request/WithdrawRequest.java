package com.gdb.transactions.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for withdrawal operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequest {

    @NotNull(message = "Account number is required")
    @Positive(message = "Account number must be positive")
    @JsonProperty("account_number")
    private Long accountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least 1.00")
    @DecimalMax(value = "1000000.00", message = "Amount cannot exceed 1,000,000.00")
    @Digits(integer = 7, fraction = 2, message = "Amount must have at most 7 integer digits and 2 decimal places")
    private BigDecimal amount;

    @NotBlank(message = "PIN is required")
    @Pattern(regexp = "\\d{4}", message = "PIN must be exactly 4 digits")
    private String pin;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
}