package com.gdb.creditcards.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * b.15 Set a per-channel spend limit for a card (e.g. ECOMMERCE, ATM).
 */
@Data
public class SetServiceLimitRequest {

    @NotBlank
    @Pattern(regexp = "ECOMMERCE|ATM|POS", message = "Channel must be ECOMMERCE, ATM or POS")
    private String channel;

    @NotNull
    @DecimalMin(value = "0.00", message = "Limit cannot be negative")
    @JsonProperty("per_txn_limit")
    private BigDecimal perTxnLimit;
}
