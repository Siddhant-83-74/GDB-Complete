package com.gdb.creditcards.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Credit card bill payment (reduces the outstanding amount, frees credit).
 */
@Data
public class PayBillRequest {

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    // Optional reference to the source instrument routed via payment gateway.
    @JsonProperty("source_reference")
    private String sourceReference;
}
