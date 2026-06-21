package com.gdb.auth.controller;

import com.gdb.auth.dto.request.LoginRequest;
import com.gdb.auth.dto.request.RefreshTokenRequest;
import com.gdb.auth.dto.response.AuthTokenResponse;
import com.gdb.auth.dto.response.TokenValidationResponse;
import com.gdb.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<AuthTokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest) {

        String ipAddress = httpServletRequest.getRemoteAddr();
        String userAgent = httpServletRequest.getHeader("User-Agent");

        return ResponseEntity.ok(authService.login(request, ipAddress, userAgent));
    }

    @PostMapping("/api/v1/auth/refresh")
    public ResponseEntity<AuthTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/api/v1/auth/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        return ResponseEntity.ok(Map.of("message", "Successfully logged out"));
    }

    @GetMapping("/api/v1/auth/me")
    public ResponseEntity<TokenValidationResponse> getCurrentProfile(
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authHeader.substring(7);
        TokenValidationResponse response = authService.validateToken(token);
        if (!response.isValid()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/internal/v1/auth/validate-token")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        return ResponseEntity.ok(authService.validateToken(token));
    }
}
