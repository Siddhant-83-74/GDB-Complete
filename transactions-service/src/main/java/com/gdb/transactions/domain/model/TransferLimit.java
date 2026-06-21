package com.gdb.transactions.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gdb.transactions.domain.enums.PrivilegeLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain model representing transfer limits for different privilege levels.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferLimit {
    private PrivilegeLevel privilege;
    @JsonProperty("daily_limit")
    private BigDecimal dailyLimit;
    @JsonProperty("per_transaction_limit")
    private BigDecimal perTransactionLimit;
    @JsonProperty("transaction_limit")
    private Integer transactionLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}