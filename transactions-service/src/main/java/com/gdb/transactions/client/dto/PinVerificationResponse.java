package com.gdb.transactions.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for PIN verification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PinVerificationResponse {
    private boolean pinValid;
}