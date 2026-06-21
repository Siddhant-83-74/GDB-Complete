package com.gdb.users.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddUserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 1, max = 255, message = "Username must be between 1 and 255 characters")
    private String username;

    @NotBlank(message = "Login ID is required")
    @Size(min = 3, max = 50, message = "Login ID must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Login ID can only contain alphanumeric characters, dots, underscores, and hyphens")
    @JsonProperty("login_id")
    private String loginId;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Pattern(regexp = "^(ADMIN|MANAGER|TELLER)$", message = "Role must be ADMIN, MANAGER, or TELLER")
    private String role;
}
