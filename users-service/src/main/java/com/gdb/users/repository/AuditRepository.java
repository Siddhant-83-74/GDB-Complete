package com.gdb.users.repository;

import com.gdb.users.domain.model.UserAuditLog;

public interface AuditRepository {
    void save(UserAuditLog auditLog);
}
