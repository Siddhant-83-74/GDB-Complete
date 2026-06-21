package com.gdb.aadhar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AadharVerificationRequest {

    @NotBlank(message = "Aadhar number is required")
    @JsonProperty("aadhar_number")
    private String aadharNumber;
}
