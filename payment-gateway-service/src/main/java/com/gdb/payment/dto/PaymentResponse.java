package com.gdb.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {

    private boolean success;

    @JsonProperty("transaction_id")
    private String transactionId;

    private String message;

    @JsonProperty("gateway_ref_id")
    private String gatewayRefId;
}
