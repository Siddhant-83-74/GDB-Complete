package com.gdb.auth.repository;

import com.gdb.auth.domain.model.AuthAuditLog;

public interface AuditRepository {
    void save(AuthAuditLog auditLog);
}
