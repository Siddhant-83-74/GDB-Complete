package com.gdb.account.controller;

import com.gdb.account.dto.request.AccountOperationRequest;
import com.gdb.account.dto.response.AccountOperationResponse;
import com.gdb.account.dto.response.AccountResponse;
import com.gdb.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Internal REST Controller for service-to-service Account operations.
 * These endpoints are used by other microservices (like transactions-service).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/internal/accounts")
@RequiredArgsConstructor
@Tag(name = "Internal Account Operations", description = "Internal API for service-to-service communication")
public class InternalAccountController {

    private final AccountService accountService;

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account details (Internal)", description = "Retrieve account information for other microservices")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<AccountResponse> getAccountDetails(@PathVariable Long accountNumber) {
        log.debug("Internal request to get account details for account: {}", accountNumber);
        AccountResponse account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/debit")
    @Operation(summary = "Debit account (Internal)", description = "Debit funds from account for transactions service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account debited successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or insufficient balance"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "422", description = "Account not active"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<AccountOperationResponse> debitAccount(@Valid @RequestBody AccountOperationRequest request) {
        log.info("Internal request to debit account: {}, amount: {}", request.getAccountNumber(), request.getAmount());
        AccountOperationResponse response = accountService.debitAccount(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/credit")
    @Operation(summary = "Credit account (Internal)", description = "Credit funds to account for transactions service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account credited successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "422", description = "Account not active"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<AccountOperationResponse> creditAccount(@Valid @RequestBody AccountOperationRequest request) {
        log.info("Internal request to credit account: {}, amount: {}", request.getAccountNumber(), request.getAmount());
        AccountOperationResponse response = accountService.creditAccount(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountNumber}/active")
    @Operation(summary = "Check account active status (Internal)", description = "Verify if account is active for transactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account status retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Boolean>> checkAccountActive(@PathVariable Long accountNumber) {
        log.debug("Internal request to check active status for account: {}", accountNumber);
        boolean isActive = accountService.isAccountActive(accountNumber);
        return ResponseEntity.ok(Map.of("active", isActive));
    }

    @GetMapping("/{accountNumber}/privilege")
    @Operation(summary = "Get account privilege (Internal)", description = "Retrieve privilege level for transaction limits")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account privilege retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, String>> getAccountPrivilege(@PathVariable Long accountNumber) {
        log.debug("Internal request to get privilege for account: {}", accountNumber);
        String privilege = accountService.getAccountPrivilege(accountNumber);
        return ResponseEntity.ok(Map.of("privilege", privilege));
    }

    @PostMapping("/{accountNumber}/verify-pin")
    @Operation(summary = "Verify PIN (Internal)", description = "Verify account PIN for transactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PIN verification completed"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Boolean>> verifyPin(
            @PathVariable Long accountNumber,
            @RequestBody Map<String, String> request) {
        log.debug("Internal request to verify PIN for account: {}", accountNumber);
        String pin = request.get("pin");
        boolean isValid = accountService.verifyPin(accountNumber, pin);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
}