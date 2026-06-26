package com.gdb.creditcards.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A single credit card transaction (purchase, bill payment or refund).
 * Used for limit accounting (b.3), high-value holds (b.6) and velocity
 * checks (b.7).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCardTransaction {
    private String id;
    private String cardId;
    private String type;          // PURCHASE / PAYMENT / REFUND
    private String channel;       // ECOMMERCE / ATM / POS / INTERNATIONAL
    private BigDecimal amount;
    private String merchant;
    private boolean international;
    private String status;        // APPROVED / ON_HOLD / DECLINED / REVERSED
    private String holdReason;    // populated when status = ON_HOLD
    private LocalDateTime createdAt;
}
