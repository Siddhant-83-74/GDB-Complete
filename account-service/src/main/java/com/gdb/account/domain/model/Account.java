package com.gdb.account.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain model representing a Bank Account.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private Long id;
    private Long accountNumber;
    private String accountType; // SAVINGS, CURRENT
    private String name;
    private String pinHash;
    private BigDecimal balance;
    private String privilege; // SILVER, GOLD, PREMIUM
    private String bankName;
    private String bankBranch;
    private String ifscCode;
    private Boolean isActive;
    private LocalDateTime activatedDate;
    private LocalDateTime closedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Type-specific details
    private SavingsDetails savingsDetails;
    private CurrentDetails currentDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavingsDetails {
        private String dateOfBirth;
        private String gender;
        private String phoneNo;
        private String aadharNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentDetails {
        private String companyName;
        private String website;
        private String registrationNo;
    }
}
