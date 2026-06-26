package com.gdb.creditcards.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Generic success envelope for status / limit / mapping operations.
 */
@Data
@Builder
public class CardOperationResponse {
    private boolean success;
    private String message;
    private String status;
}
