package com.gdb.users.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyCredentialsResponse {
    @JsonProperty("isValid")
    private boolean isValid;
    private Long userId;
    private String role;
    @JsonProperty("isActive")
    private boolean isActive;
}
