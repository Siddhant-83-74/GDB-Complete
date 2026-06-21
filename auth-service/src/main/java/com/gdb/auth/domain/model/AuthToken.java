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
public class AuthToken {
    private UUID id;
    private Long userId;
    private String loginId;
    private String tokenJti;
    private OffsetDateTime issuedAt;
    private OffsetDateTime expiresAt;
    private Boolean isRevoked;
    private OffsetDateTime createdAt;
}
