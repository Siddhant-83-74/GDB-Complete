package com.gdb.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Login ID is required")
    @JsonProperty("login_id")
    private String loginId;

    @NotBlank(message = "Password is required")
    private String password;
}
