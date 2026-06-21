package com.gdb.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {
    @JsonProperty("valid")
    private boolean isValid;
    @JsonProperty("user_id")
    private Long userId;
    @JsonProperty("login_id")
    private String loginId;
    private String role;
    @JsonProperty("revoked")
    private boolean revoked;
    @JsonProperty("expires_at")
    private OffsetDateTime expiresAt;
}
