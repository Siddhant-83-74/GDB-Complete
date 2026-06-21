package com.gdb.users.service;

public interface AuditService {
    void logAction(Long userId, String action, Object oldData, Object newData);
}
