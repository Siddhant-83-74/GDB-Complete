package com.gdb.users.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EditUserRequest {

    @Size(min = 1, max = 255, message = "Username must be between 1 and 255 characters")
    private String username;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Pattern(regexp = "^(ADMIN|MANAGER|TELLER)$", message = "Role must be ADMIN, MANAGER, or TELLER")
    private String role;
}
