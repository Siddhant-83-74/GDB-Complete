package com.gdb.creditcards.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * b.14 Map (link) a credit card to an existing bank account.
 */
@Data
public class MapAccountRequest {

    @NotNull(message = "Account number is required")
    @JsonProperty("account_number")
    private Long accountNumber;
}
