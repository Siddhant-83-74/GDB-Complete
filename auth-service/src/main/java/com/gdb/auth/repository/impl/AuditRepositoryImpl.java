package com.gdb.auth.repository.impl;

import com.gdb.auth.domain.model.AuthAuditLog;
import com.gdb.auth.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuditRepositoryImpl implements AuditRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(AuthAuditLog auditLog) {
        String sql = "INSERT INTO auth_audit_logs (login_id, user_id, action, reason, ip_address, user_agent) " +
                "VALUES (?, ?, ?::auth_action_enum, ?, ?::inet, ?)";
        jdbcTemplate.update(sql,
                auditLog.getLoginId(),
                auditLog.getUserId(),
                auditLog.getAction(),
                auditLog.getReason(),
                auditLog.getIpAddress(),
                auditLog.getUserAgent());
    }
}
