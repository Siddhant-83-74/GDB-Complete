package com.gdb.creditcards.repository;

import com.gdb.creditcards.domain.model.CreditCardTransaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CreditCardTransactionRepository {

    CreditCardTransaction save(CreditCardTransaction txn);

    Optional<CreditCardTransaction> findById(String id);

    List<CreditCardTransaction> findByCardId(String cardId);

    void updateStatus(String id, String status);

    /** b.7 Count non-declined transactions on a card since {@code since}. */
    int countSince(String cardId, LocalDateTime since);
}
