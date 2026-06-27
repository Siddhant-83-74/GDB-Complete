package com.gdb.creditcards.controller;

import com.gdb.creditcards.dto.request.*;
import com.gdb.creditcards.dto.response.CardOperationResponse;
import com.gdb.creditcards.dto.response.CreditCardResponse;
import com.gdb.creditcards.dto.response.TransactionResponse;
import com.gdb.creditcards.service.CreditCardService;
import com.gdb.creditcards.service.CreditCardTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/credit-cards")
@RequiredArgsConstructor
public class CreditCardController {

    private final CreditCardService cardService;
    private final CreditCardTransactionService transactionService;

    // --- Application & retrieval ---

    @PostMapping("/apply")
    public ResponseEntity<CreditCardResponse> applyForCard(@Valid @RequestBody ApplyCardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.apply(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CreditCardResponse>> listUserCards(@PathVariable Long userId) {
        return ResponseEntity.ok(cardService.listByUser(userId));
    }

    /** Whole-portfolio listing for the admin card selector (single source of truth with analytics). */
    @GetMapping
    public ResponseEntity<List<CreditCardResponse>> listAllCards() {
        return ResponseEntity.ok(cardService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditCardResponse> getCardDetails(@PathVariable String id) {
        return ResponseEntity.ok(cardService.getById(id));
    }

    // --- Transactions ---

    @PostMapping("/{id}/transactions")
    public ResponseEntity<TransactionResponse> transact(@PathVariable String id,
            @Valid @RequestBody CardTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.transact(id, request));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionResponse>> getCardTransactions(@PathVariable String id) {
        return ResponseEntity.ok(transactionService.listByCard(id));
    }

    @PostMapping("/transactions/{transactionId}/confirm")
    public ResponseEntity<TransactionResponse> confirmHeldTransaction(@PathVariable String transactionId) {
        return ResponseEntity.ok(transactionService.confirmHold(transactionId));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<TransactionResponse> payCreditCardBill(@PathVariable String id,
            @Valid @RequestBody PayBillRequest request) {
        return ResponseEntity.ok(transactionService.payBill(id, request));
    }

    // --- Card controls ---

    @PatchMapping("/{id}/status")
    public ResponseEntity<CardOperationResponse> updateStatus(@PathVariable String id,
            @Valid @RequestBody CardStatusRequest request) {
        return ResponseEntity.ok(cardService.updateStatus(id, request.getStatus()));
    }

    @PutMapping("/{id}/service-limits")
    public ResponseEntity<CardOperationResponse> setServiceLimit(@PathVariable String id,
            @Valid @RequestBody SetServiceLimitRequest request) {
        return ResponseEntity.ok(cardService.setServiceLimit(id, request));
    }

    @PatchMapping("/{id}/international")
    public ResponseEntity<CardOperationResponse> toggleInternational(@PathVariable String id,
            @Valid @RequestBody InternationalToggleRequest request) {
        return ResponseEntity.ok(cardService.toggleInternational(id, request.getEnabled()));
    }

    @PutMapping("/{id}/account")
    public ResponseEntity<CardOperationResponse> mapAccount(@PathVariable String id,
            @Valid @RequestBody MapAccountRequest request) {
        return ResponseEntity.ok(cardService.mapAccount(id, request.getAccountNumber()));
    }
}
