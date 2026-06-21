package com.gdb.transactions.domain.model;

import com.gdb.transactions.domain.enums.TransferMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain model representing a fund transfer between accounts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundTransfer {
    private Long id;
    private Long fromAccount;
    private Long toAccount;
    private BigDecimal transferAmount;
    private TransferMode transferMode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}