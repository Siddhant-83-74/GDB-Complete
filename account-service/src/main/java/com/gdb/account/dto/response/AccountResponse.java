package com.gdb.account.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    @JsonProperty("activated_date")
    private LocalDateTime activatedDate;
    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("savings_details")
    private SavingsDetails savingsDetails;
    @JsonProperty("current_details")
    private CurrentDetails currentDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavingsDetails {
        @JsonProperty("date_of_birth")
        private String dateOfBirth;
        private String gender;
        @JsonProperty("phone_no")
        private String phoneNo;
        @JsonProperty("aadhar_number")
        private String aadharNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentDetails {
        @JsonProperty("company_name")
        private String companyName;
        private String website;
        @JsonProperty("registration_no")
        private String registrationNo;
    }
}
