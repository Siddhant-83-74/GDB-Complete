package com.gdb.transactions.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO from Account Service for account information.
 * Uses @JsonIgnoreProperties to ignore extra fields from account service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountResponse {
    @JsonProperty("account_number")
    private Long accountNumber;

    @JsonProperty("account_type")
    private String accountType;

    private String name;
    private BigDecimal balance;
    private String privilege;

    @JsonProperty("bank_name")
    private String bankName;

    @JsonProperty("bank_branch")
    private String bankBranch;

    @JsonProperty("ifsc_code")
    private String ifscCode;

    @JsonProperty("is_active")
    private Boolean isActive;

    // Ignore nested objects for now - we only need basic account info
}