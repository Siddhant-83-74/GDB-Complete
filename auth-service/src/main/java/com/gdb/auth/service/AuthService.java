package com.gdb.auth.service;

import com.gdb.auth.dto.request.LoginRequest;
import com.gdb.auth.dto.request.RefreshTokenRequest;
import com.gdb.auth.dto.response.AuthTokenResponse;
import com.gdb.auth.dto.response.TokenValidationResponse;

public interface AuthService {
    AuthTokenResponse login(LoginRequest request, String ipAddress, String userAgent);

    AuthTokenResponse refresh(RefreshTokenRequest request);

    void logout(String token);

    TokenValidationResponse validateToken(String token);
}
