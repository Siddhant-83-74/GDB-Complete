package com.gdb.creditcards.repository;

import com.gdb.creditcards.domain.model.CardServiceLimit;
import com.gdb.creditcards.domain.model.CreditCard;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for credit cards and their per-channel service limits.
 */
public interface CreditCardRepository {

    CreditCard save(CreditCard card);

    Optional<CreditCard> findById(String id);

    List<CreditCard> findByUserId(Long userId);

    /** Every card in the portfolio (admin command center). */
    List<CreditCard> findAll();

    void updateBalances(String id, BigDecimal availableCredit, BigDecimal outstandingAmount);

    void updateStatus(String id, String status);

    void updateInternational(String id, boolean enabled);

    void linkAccount(String id, Long accountNumber);

    // b.15 service limits
    void upsertServiceLimit(CardServiceLimit limit);

    List<CardServiceLimit> findServiceLimits(String cardId);

    Optional<CardServiceLimit> findServiceLimit(String cardId, String channel);
}
