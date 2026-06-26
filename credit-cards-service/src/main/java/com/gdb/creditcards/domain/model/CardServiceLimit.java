package com.gdb.creditcards.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Per-channel spend limit for a card (b.15) e.g. ECOMMERCE, ATM, POS.
 * A {@code null}/absent row means "no channel-specific cap" and only the
 * overall credit limit applies.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardServiceLimit {
    private String id;
    private String cardId;
    private String channel;          // ECOMMERCE / ATM / POS
    private BigDecimal perTxnLimit;  // max amount per single transaction
}
