package com.gdb.company.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyVerificationResponse {

    @JsonProperty("registration_number")
    private String registrationNumber;

    @JsonProperty("is_valid")
    private boolean isValid;

    @JsonProperty("company_name")
    private String companyName;

    private String type;

    private String address;

    private String email;

    private String phone;

    private String website;

    @JsonProperty("incorporation_date")
    private String incorporationDate;

    @JsonProperty("paid_up_capital")
    private String paidUpCapital;

    private java.util.List<String> directors;

    private String status;
    private String message;
    private LocalDateTime timestamp;
}
