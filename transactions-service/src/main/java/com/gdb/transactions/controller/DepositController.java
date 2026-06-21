package com.gdb.transactions.controller;

import com.gdb.transactions.dto.request.DepositRequest;
import com.gdb.transactions.dto.response.TransactionResponse;
import com.gdb.transactions.service.DepositService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.gdb.transactions.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for deposit operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/transactions/deposit")
@RequiredArgsConstructor
@Tag(name = "Deposits", description = "Deposit operations")
public class DepositController {

    private final DepositService depositService;

    // TODO: MOD6-BUG-01: Missing Validation on payload.
    // Trainee task: Observe that negative deposit amounts are being processed without validation errors.
    // Identify which Spring annotation is missing from the method parameter to trigger input validation constraints.
    // Injected Bug: Missing the @Valid annotation on the request payload.
    @PostMapping
    @Operation(summary = "Process deposit", description = "Process a deposit to an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Deposit processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "422", description = "Validation error"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<TransactionResponse> processDeposit(@Valid @RequestBody DepositRequest request) {
        SecurityUtils.checkStaffRole();
        log.info("Received deposit request for account: {}", request.getAccountNumber());

        TransactionResponse response = depositService.processDeposit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}