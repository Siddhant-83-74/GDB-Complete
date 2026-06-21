package com.gdb.users.repository.impl;

import com.gdb.users.domain.model.UserAuditLog;
import com.gdb.users.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuditRepositoryImpl implements AuditRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(UserAuditLog auditLog) {
        String sql = "INSERT INTO user_audit_log (user_id, action, old_data, new_data) VALUES (?, ?::audit_action_enum, ?::jsonb, ?::jsonb)";

        jdbcTemplate.update(sql,
                auditLog.getUserId(),
                auditLog.getAction(),
                auditLog.getOldData(),
                auditLog.getNewData());
    }
}
