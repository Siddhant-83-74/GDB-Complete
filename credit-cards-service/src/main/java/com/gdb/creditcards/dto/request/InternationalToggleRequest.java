package com.gdb.creditcards.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * b.16 Enable / disable international transactions on a card.
 */
@Data
public class InternationalToggleRequest {

    @NotNull(message = "enabled flag is required")
    private Boolean enabled;
}
