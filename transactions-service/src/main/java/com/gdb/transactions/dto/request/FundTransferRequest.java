package com.gdb.transactions.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gdb.transactions.domain.enums.TransferMode;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for fund transfer operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundTransferRequest {

    @NotNull(message = "From account is required")
    @Positive(message = "From account must be positive")
    @JsonProperty("from_account")
    private Long fromAccount;

    @NotNull(message = "To account is required")
    @Positive(message = "To account must be positive")
    @JsonProperty("to_account")
    private Long toAccount;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least 1.00")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal amount;

    @NotBlank(message = "PIN is required")
    @Pattern(regexp = "\\d{4}", message = "PIN must be exactly 4 digits")
    private String pin;

    @NotNull(message = "Transfer mode is required")
    @JsonProperty("transfer_mode")
    private TransferMode transferMode;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
}