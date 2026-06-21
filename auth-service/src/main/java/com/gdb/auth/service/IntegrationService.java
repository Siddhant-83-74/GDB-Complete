package com.gdb.auth.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface IntegrationService {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class UserVerificationResponse {
        @JsonProperty("isValid")
        private boolean isValid;
        private Long userId;
        private String role;
        @JsonProperty("isActive")
        private boolean isActive;
    }

    UserVerificationResponse verifyCredentials(String loginId, String password);

    UserVerificationResponse getUserStatus(String loginId);
}
