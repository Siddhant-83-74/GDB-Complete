package com.gdb.transactions.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for PIN verification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PinVerificationRequest {
    private String pin;
}