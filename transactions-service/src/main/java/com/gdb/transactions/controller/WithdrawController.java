package com.gdb.transactions.controller;

import com.gdb.transactions.dto.request.WithdrawRequest;
import com.gdb.transactions.dto.response.TransactionResponse;
import com.gdb.transactions.service.WithdrawService;
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
 * REST controller for withdrawal operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/transactions/withdraw")
@RequiredArgsConstructor
@Tag(name = "Withdrawals", description = "Withdrawal operations")
public class WithdrawController {

    private final WithdrawService withdrawService;

    @PostMapping
    @Operation(summary = "Process withdrawal", description = "Process a withdrawal from an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Withdrawal processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or insufficient funds"),
            @ApiResponse(responseCode = "401", description = "Invalid PIN"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "422", description = "Validation error"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<TransactionResponse> processWithdrawal(@Valid @RequestBody WithdrawRequest request) {
        SecurityUtils.checkStaffRole();
        log.info("Received withdrawal request for account: {}", request.getAccountNumber());

        TransactionResponse response = withdrawService.processWithdraw(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}