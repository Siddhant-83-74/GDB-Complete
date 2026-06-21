package com.gdb.transactions.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gdb.transactions.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for transaction operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String status;
    @JsonProperty("transaction_id")
    private Long transactionId;
    @JsonProperty("account_number")
    private Long accountNumber;
    @JsonProperty("amount")
    private BigDecimal amount;
    @JsonProperty("transaction_type")
    private TransactionType transactionType;
    @JsonProperty("description")
    private String description;
    @JsonProperty("balance_after")
    private BigDecimal newBalance;
    @JsonProperty("created_at")
    private LocalDateTime transactionDate;
}