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
 * Response DTO for transaction log entries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLogResponse {
    private Long id;
    @JsonProperty("account_number")
    private Long accountNumber;
    private BigDecimal amount;
    @JsonProperty("transaction_type")
    private TransactionType transactionType;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}