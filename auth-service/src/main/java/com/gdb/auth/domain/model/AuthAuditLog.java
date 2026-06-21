package com.gdb.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthAuditLog {
    private UUID id;
    private String loginId;
    private Long userId;
    private String action; // Using String for simplicity with JdbcTemplate and Enum mapping
    private String reason;
    private String ipAddress;
    private String userAgent;
    private OffsetDateTime createdAt;
}
