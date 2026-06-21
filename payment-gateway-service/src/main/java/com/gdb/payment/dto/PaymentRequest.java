package com.gdb.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {

    @NotNull(message = "Source account ID is required")
    @JsonProperty("source_account_id")
    private Long sourceAccountId;

    @NotNull(message = "Destination account ID is required")
    @JsonProperty("destination_account_id")
    private Long destinationAccountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Payment mode is required")
    private PaymentMode mode;

    @JsonProperty("reference_id")
    private String referenceId;
}
