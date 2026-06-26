package com.gdb.creditcards.service;

import com.gdb.creditcards.domain.model.CreditCard;
import com.gdb.creditcards.dto.request.ApplyCardRequest;
import com.gdb.creditcards.dto.request.SetServiceLimitRequest;
import com.gdb.creditcards.dto.response.CardOperationResponse;
import com.gdb.creditcards.dto.response.CreditCardResponse;

import java.util.List;

public interface CreditCardService {

    /** b.1-b.5, b.8, b.10-b.13: validate, generate and persist a new card. */
    CreditCardResponse apply(ApplyCardRequest request);

    List<CreditCardResponse> listByUser(Long userId);

    CreditCardResponse getById(String id);

    /** b.9 enable/disable/block a card. */
    CardOperationResponse updateStatus(String id, String status);

    /** b.15 set a per-channel spend limit. */
    CardOperationResponse setServiceLimit(String id, SetServiceLimitRequest request);

    /** b.16 enable/disable international transactions. */
    CardOperationResponse toggleInternational(String id, boolean enabled);

    /** b.14 link a card to a bank account (verified via Account Service). */
    CardOperationResponse mapAccount(String id, Long accountNumber);

    /** Loads a card or throws CARD_NOT_FOUND. Shared by the transaction service. */
    CreditCard requireCard(String id);
}
