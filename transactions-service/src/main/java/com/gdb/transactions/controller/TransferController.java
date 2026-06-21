package com.gdb.transactions.controller;

import com.gdb.transactions.dto.request.FundTransferRequest;
import com.gdb.transactions.dto.response.FundTransferResponse;
import com.gdb.transactions.service.TransferService;
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
 * REST controller for fund transfer operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/transactions/transfer")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Fund transfer operations")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @Operation(summary = "Process fund transfer", description = "Process a fund transfer between accounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transfer processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request, insufficient funds, or limit exceeded"),
            @ApiResponse(responseCode = "401", description = "Invalid PIN"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "422", description = "Validation error"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<FundTransferResponse> processTransfer(@Valid @RequestBody FundTransferRequest request) {
        SecurityUtils.checkStaffRole();
        log.info("Received transfer request from account: {} to account: {}",
                request.getFromAccount(), request.getToAccount());

        FundTransferResponse response = transferService.processTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}