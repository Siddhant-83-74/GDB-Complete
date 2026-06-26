package com.gdb.creditcards.controller;

import com.gdb.creditcards.dto.response.CreditCardResponse;
import com.gdb.creditcards.service.CreditCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Service-to-service endpoints (no end-user auth; allowed through the
 * SecurityFilter via the /api/v1/internal/ prefix).
 */
@RestController
@RequestMapping("/api/v1/internal/credit-cards")
@RequiredArgsConstructor
public class InternalCreditCardController {

    private final CreditCardService cardService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CreditCardResponse>> listUserCards(@PathVariable Long userId) {
        return ResponseEntity.ok(cardService.listByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditCardResponse> getCard(@PathVariable String id) {
        return ResponseEntity.ok(cardService.getById(id));
    }
}
