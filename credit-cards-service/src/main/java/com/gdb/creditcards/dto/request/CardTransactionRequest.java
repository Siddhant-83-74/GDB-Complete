package com.gdb.creditcards.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * A purchase / spend request against a credit card.
 */
@Data
public class CardTransactionRequest {

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank
    @Pattern(regexp = "ECOMMERCE|ATM|POS|INTERNATIONAL", message = "Unsupported channel")
    private String channel;

    @Size(max = 255)
    private String merchant;

    // b.16 marks the transaction as cross-border
    private boolean international;
}
