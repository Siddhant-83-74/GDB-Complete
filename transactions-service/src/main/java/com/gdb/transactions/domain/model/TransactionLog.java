package com.gdb.transactions.domain.model;

import com.gdb.transactions.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain model representing a transaction log entry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLog {
    private Long id;
    private Long accountNumber;
    private BigDecimal amount;
    private TransactionType transactionType;
    private Long referenceId;
    private String description;
    private String mode;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}