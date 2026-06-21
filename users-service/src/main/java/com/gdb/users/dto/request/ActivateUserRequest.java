package com.gdb.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActivateUserRequest {
    @NotBlank(message = "Login ID is required")
    private String loginId;
}
