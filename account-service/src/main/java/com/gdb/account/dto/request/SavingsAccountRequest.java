package com.gdb.account.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SavingsAccountRequest {
    @NotBlank
    @Size(min = 2, max = 255)
    private String name;

    @NotBlank
    @Pattern(regexp = "^\\d{4}$", message = "PIN must be 4 digits")
    private String pin;

    @NotBlank
    @JsonProperty("date_of_birth")
    private String dateOfBirth; // YYYY-MM-DD

    @NotBlank
    @Pattern(regexp = "Male|Female|Others")
    private String gender;

    @NotBlank
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits")
    @JsonProperty("phone_no")
    private String phoneNo;

    @NotBlank
    @Pattern(regexp = "^\\d{12}$", message = "Aadhar number must be 12 digits")
    @JsonProperty("aadhar_number")
    private String aadharNumber;

    @NotBlank
    @Pattern(regexp = "SILVER|GOLD|PREMIUM")
    private String privilege;

    @NotNull
    @DecimalMin(value = "2000", message = "Initial deposit must be at least ₹2000")
    @JsonProperty("initial_balance")
    private BigDecimal initialBalance;
}
