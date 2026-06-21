package com.gdb.users.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdb.users.domain.model.UserAuditLog;
import com.gdb.users.repository.AuditRepository;
import com.gdb.users.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void logAction(Long userId, String action, Object oldData, Object newData) {
        try {
            String oldDataJson = oldData != null ? objectMapper.writeValueAsString(oldData) : null;
            String newDataJson = newData != null ? objectMapper.writeValueAsString(newData) : null;

            UserAuditLog auditLog = UserAuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .oldData(oldDataJson)
                    .newData(newDataJson)
                    .build();

            auditRepository.save(auditLog);
            log.debug("Audit log saved: {} for user {}", action, userId);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage(), e);
        }
    }
}
