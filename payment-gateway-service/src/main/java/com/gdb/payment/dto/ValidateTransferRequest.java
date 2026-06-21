package com.gdb.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ValidateTransferRequest {

    @JsonProperty("from_account")
    private Long fromAccount;

    @JsonProperty("to_account")
    private Long toAccount;

    private BigDecimal amount;

    @JsonProperty("transaction_id")
    private String transactionId;
}
