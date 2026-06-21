package com.gdb.aadhar.dto;

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
public class AadharVerificationResponse {

    @JsonProperty("aadhar_number")
    private String aadharNumber;

    @JsonProperty("is_valid")
    private boolean isValid;

    private String name;

    @JsonProperty("mobile_no")
    private String mobileNo;

    private String address;

    private String gender;

    @JsonProperty("date_of_birth")
    private String dateOfBirth;

    @JsonProperty("photo_url")
    private String photoUrl;

    private String status;

    private String message;

    private LocalDateTime timestamp;
}
