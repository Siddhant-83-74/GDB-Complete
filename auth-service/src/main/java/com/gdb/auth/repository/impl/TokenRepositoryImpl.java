package com.gdb.auth.repository.impl;

import com.gdb.auth.domain.model.AuthToken;
import com.gdb.auth.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TokenRepositoryImpl implements TokenRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<AuthToken> tokenRowMapper = (rs, rowNum) -> AuthToken.builder()
            .id(rs.getObject("id", java.util.UUID.class))
            .userId(rs.getLong("user_id"))
            .loginId(rs.getString("login_id"))
            .tokenJti(rs.getString("token_jti"))
            .issuedAt(rs.getTimestamp("issued_at").toInstant().atOffset(java.time.ZoneOffset.UTC))
            .expiresAt(rs.getTimestamp("expires_at").toInstant().atOffset(java.time.ZoneOffset.UTC))
            .isRevoked(rs.getBoolean("is_revoked"))
            .createdAt(rs.getTimestamp("created_at").toInstant().atOffset(java.time.ZoneOffset.UTC))
            .build();

    @Override
    public void save(AuthToken token) {
        String sql = "INSERT INTO auth_tokens (user_id, login_id, token_jti, issued_at, expires_at) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                token.getUserId(),
                token.getLoginId(),
                token.getTokenJti(),
                java.sql.Timestamp.from(token.getIssuedAt().toInstant()),
                java.sql.Timestamp.from(token.getExpiresAt().toInstant()));
    }

    @Override
    public Optional<AuthToken> findByJti(String jti) {
        String sql = "SELECT * FROM auth_tokens WHERE token_jti = ?";
        return jdbcTemplate.query(sql, tokenRowMapper, jti).stream().findFirst();
    }

    @Override
    public void revokeByJti(String jti) {
        String sql = "UPDATE auth_tokens SET is_revoked = TRUE WHERE token_jti = ?";
        jdbcTemplate.update(sql, jti);
    }

    @Override
    public void revokeAllByUserId(Long userId) {
        String sql = "UPDATE auth_tokens SET is_revoked = TRUE WHERE user_id = ? AND is_revoked = FALSE";
        jdbcTemplate.update(sql, userId);
    }
}
