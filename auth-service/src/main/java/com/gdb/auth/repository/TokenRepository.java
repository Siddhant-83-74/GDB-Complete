package com.gdb.auth.repository;

import com.gdb.auth.domain.model.AuthToken;
import java.util.Optional;

public interface TokenRepository {
    void save(AuthToken token);

    Optional<AuthToken> findByJti(String jti);

    void revokeByJti(String jti);

    void revokeAllByUserId(Long userId);
}
