package com.gdb.users.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuditLog {
    private Long auditId;
    private Long userId;
    private String action;
    private String oldData;
    private String newData;
    private LocalDateTime timestamp;
}
