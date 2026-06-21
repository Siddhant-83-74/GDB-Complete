package com.gdb.account.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CurrentAccountRequest {
    @NotBlank
    @Size(min = 1, max = 255)
    private String name;

    @NotBlank
    @Pattern(regexp = "^\\d{4}$", message = "PIN must be 4 digits")
    private String pin;

    @NotBlank
    @Size(min = 1, max = 255)
    @JsonProperty("company_name")
    private String companyName;

    @NotBlank
    @Size(min = 1, max = 50)
    @JsonProperty("registration_no")
    private String registrationNo;

    private String website;

    @NotBlank
    @Pattern(regexp = "SILVER|GOLD|PREMIUM")
    private String privilege;
}
