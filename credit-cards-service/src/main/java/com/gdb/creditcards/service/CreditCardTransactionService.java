package com.gdb.creditcards.service;

import com.gdb.creditcards.dto.request.CardTransactionRequest;
import com.gdb.creditcards.dto.request.PayBillRequest;
import com.gdb.creditcards.dto.response.TransactionResponse;

import java.util.List;

public interface CreditCardTransactionService {

    /** b.3, b.6, b.7, b.15, b.16: run a spend against a card. */
    TransactionResponse transact(String cardId, CardTransactionRequest request);

    /** b.6 confirm a held high-value transaction. */
    TransactionResponse confirmHold(String transactionId);

    /** Pay down the outstanding balance, freeing available credit. */
    TransactionResponse payBill(String cardId, PayBillRequest request);

    List<TransactionResponse> listByCard(String cardId);
}
