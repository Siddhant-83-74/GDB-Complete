package com.gdb.users.security;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserContext {
    private Long userId;
    private String loginId;
    private String role;
}
