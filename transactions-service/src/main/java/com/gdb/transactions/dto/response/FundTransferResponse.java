package com.gdb.transactions.dto.response;

import com.gdb.transactions.domain.enums.TransferMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for fund transfer operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundTransferResponse {
    private String status;
    private Long transactionId;
    private Long fromAccount;
    private Long toAccount;
    private BigDecimal amount;
    private TransferMode transferMode;
    private String description;
    private BigDecimal fromAccountNewBalance;
    private BigDecimal toAccountNewBalance;
    private LocalDateTime transactionDate;
}