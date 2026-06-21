package com.gdb.transactions.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gdb.transactions.domain.enums.PrivilegeLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for transfer limit information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferLimitResponse {
    @JsonProperty("account_number")
    private Long accountNumber;
    private PrivilegeLevel privilege;
    @JsonProperty("daily_limit")
    private BigDecimal dailyLimit;
    @JsonProperty("daily_used")
    private BigDecimal dailyUsed;
    @JsonProperty("daily_remaining")
    private BigDecimal dailyRemaining;
    @JsonProperty("transaction_limit")
    private Integer transactionLimit;
    @JsonProperty("transactions_today")
    private Integer transactionsToday;
    @JsonProperty("transactions_remaining")
    private Integer transactionsRemaining;
}