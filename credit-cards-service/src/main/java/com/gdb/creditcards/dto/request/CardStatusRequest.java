package com.gdb.creditcards.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * b.9 Enable / disable (or block) a credit card.
 */
@Data
public class CardStatusRequest {

    @NotBlank
    @Pattern(regexp = "ACTIVE|INACTIVE|BLOCKED", message = "Status must be ACTIVE, INACTIVE or BLOCKED")
    private String status;
}
