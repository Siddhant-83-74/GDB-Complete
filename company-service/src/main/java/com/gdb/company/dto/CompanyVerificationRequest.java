package com.gdb.company.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyVerificationRequest {

    @NotBlank(message = "Registration number is required")
    @Size(min = 21, max = 21, message = "Registration number must be exactly 21 characters")
    @Pattern(regexp = "^[UL]\\d{5}[A-Z]{2}\\d{4}[A-Z]{3}\\d{6}$", message = "Invalid Company Identification Number (CIN) format")
    @JsonProperty("registration_number")
    private String registrationNumber;
}
