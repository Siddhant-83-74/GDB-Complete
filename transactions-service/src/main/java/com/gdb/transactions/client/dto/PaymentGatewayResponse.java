package com.gdb.transactions.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO from the Central Payment Gateway Service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayResponse {

    private boolean success;

    @JsonProperty("transaction_id")
    private String transactionId;

    private String message;

    @JsonProperty("gateway_ref_id")
    private String gatewayRefId;
}
