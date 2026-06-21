package com.gdb.auth.service.impl;

import com.gdb.auth.domain.model.AuthAction;
import com.gdb.auth.domain.model.AuthAuditLog;
import com.gdb.auth.domain.model.AuthToken;
import com.gdb.auth.dto.request.LoginRequest;
import com.gdb.auth.dto.request.RefreshTokenRequest;
import com.gdb.auth.dto.response.AuthTokenResponse;
import com.gdb.auth.dto.response.TokenValidationResponse;
import com.gdb.auth.repository.AuditRepository;
import com.gdb.auth.repository.TokenRepository;
import com.gdb.auth.service.AuthService;
import com.gdb.auth.service.IntegrationService;
import com.gdb.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserAuthServiceImpl implements AuthService {

    private final IntegrationService integrationService;
    private final TokenRepository tokenRepository;
    private final AuditRepository auditRepository;
    private final JwtUtil jwtUtil;

    @Override
    public AuthTokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        log.info("Attempting login for user: {}", request.getLoginId());

        IntegrationService.UserVerificationResponse verification = integrationService
                .verifyCredentials(request.getLoginId(), request.getPassword());

        if (verification == null || !verification.isValid()) {
            logAudit(request.getLoginId(), null, AuthAction.LOGIN_FAILURE, "Invalid credentials", ipAddress, userAgent);
            throw new RuntimeException("AUTHENTICATION_FAILED");
        }

        if (!verification.isActive()) {
            logAudit(request.getLoginId(), verification.getUserId(), AuthAction.LOGIN_FAILURE,
                    "User account is inactive", ipAddress, userAgent);
            throw new RuntimeException("USER_LOCKED");
        }

        String accessToken = jwtUtil.generateAccessToken(verification.getUserId(), request.getLoginId(),
                verification.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(verification.getUserId(), request.getLoginId());

        // Save refresh token metadata for revocation support
        AuthToken tokenEntity = AuthToken.builder()
                .userId(verification.getUserId())
                .loginId(request.getLoginId())
                .tokenJti(jwtUtil.extractJti(refreshToken))
                .issuedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .expiresAt(jwtUtil.extractExpiration(refreshToken).toInstant().atOffset(ZoneOffset.UTC))
                .build();
        tokenRepository.save(tokenEntity);

        logAudit(request.getLoginId(), verification.getUserId(), AuthAction.LOGIN_SUCCESS, null, ipAddress, userAgent);

        return AuthTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(30L * 60L) // matches 30 mins
                .user(AuthTokenResponse.UserSummary.builder()
                        .userId(verification.getUserId())
                        .loginId(request.getLoginId())
                        .role(verification.getRole())
                        .build())
                .build();
    }

    @Override
    public AuthTokenResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("REFRESH_TOKEN_INVALID");
        }

        String jti = jwtUtil.extractJti(refreshToken);
        Optional<AuthToken> tokenOpt = tokenRepository.findByJti(jti);

        if (tokenOpt.isEmpty() || tokenOpt.get().getIsRevoked()) {
            throw new RuntimeException("REFRESH_TOKEN_INVALID");
        }

        String loginId = jwtUtil.extractUsername(refreshToken);
        IntegrationService.UserVerificationResponse status = integrationService.getUserStatus(loginId);

        if (status == null || !status.isActive()) {
            throw new RuntimeException("USER_LOCKED");
        }

        String newAccessToken = jwtUtil.generateAccessToken(status.getUserId(), loginId, status.getRole());

        // Refresh token rotation (optional but recommended)
        String newRefreshToken = jwtUtil.generateRefreshToken(status.getUserId(), loginId);
        tokenRepository.revokeByJti(jti);

        AuthToken newTokenEntity = AuthToken.builder()
                .userId(status.getUserId())
                .loginId(loginId)
                .tokenJti(jwtUtil.extractJti(newRefreshToken))
                .issuedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .expiresAt(jwtUtil.extractExpiration(newRefreshToken).toInstant().atOffset(ZoneOffset.UTC))
                .build();
        tokenRepository.save(newTokenEntity);

        return AuthTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(30L * 60L)
                .user(AuthTokenResponse.UserSummary.builder()
                        .userId(status.getUserId())
                        .loginId(loginId)
                        .role(status.getRole())
                        .build())
                .build();
    }

    @Override
    public void logout(String token) {
        try {
            String jti = jwtUtil.extractJti(token);
            tokenRepository.revokeByJti(jti);
            log.info("Token revoked for logout: {}", jti);
        } catch (Exception e) {
            log.warn("Logout attempt with invalid or expired token");
        }
    }

    @Override
    public TokenValidationResponse validateToken(String token) {
        boolean isValid = jwtUtil.validateToken(token);
        if (!isValid) {
            return TokenValidationResponse.builder().isValid(false).build();
        }

        // MOD8-CR-01: reject blacklisted/revoked tokens even if the signature is still valid
        String jti = jwtUtil.extractJti(token);
        boolean revoked = Boolean.TRUE.equals(
                tokenRepository.findByJti(jti).map(AuthToken::getIsRevoked).orElse(false));

        return TokenValidationResponse.builder()
                .isValid(!revoked)
                .revoked(revoked)
                .userId(jwtUtil.extractUserId(token))
                .loginId(jwtUtil.extractUsername(token))
                .role(jwtUtil.extractRole(token))
                .expiresAt(jwtUtil.extractExpiration(token).toInstant().atOffset(ZoneOffset.UTC))
                .build();
    }

    private void logAudit(String loginId, Long userId, String action, String reason, String ip, String ua) {
        auditRepository.save(AuthAuditLog.builder()
                .loginId(loginId)
                .userId(userId)
                .action(action)
                .reason(reason)
                .ipAddress(ip)
                .userAgent(ua)
                .build());
    }
}
