package com.gdb.transactions.controller;

import com.gdb.transactions.domain.model.TransferLimit;
import com.gdb.transactions.dto.request.LimitCheckRequest;
import com.gdb.transactions.dto.response.TransferLimitResponse;
import com.gdb.transactions.service.TransferLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.gdb.transactions.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for transfer limit operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/transfer-limits")
@RequiredArgsConstructor
@Tag(name = "Transfer Limits", description = "Transfer limit operations")
public class TransferLimitController {

        private final TransferLimitService transferLimitService;

        @GetMapping
        @Operation(summary = "Get transfer limit", description = "Get transfer limit information for an account")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Transfer limit retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Account not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<TransferLimitResponse> getTransferLimit(
                        @Parameter(description = "Account number", required = true) @RequestParam Long accountNumber) {
                SecurityUtils.checkStaffRole();
                log.info("Received request to get transfer limit for account: {}", accountNumber);

                TransferLimitResponse response = transferLimitService.getTransferLimit(accountNumber);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/remaining")
        @Operation(summary = "Get remaining limit", description = "Get remaining transfer limit for today")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Remaining limit retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Account not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<TransferLimitResponse> getRemainingLimit(
                        @Parameter(description = "Account number", required = true) @RequestParam Long accountNumber) {
                SecurityUtils.checkStaffRole();
                log.info("Received request to get remaining limit for account: {}", accountNumber);

                TransferLimitResponse response = transferLimitService.getRemainingLimit(accountNumber);
                return ResponseEntity.ok(response);
        }

        @GetMapping({ "/rules", "/rules/all" })
        @Operation(summary = "Get all transfer rules", description = "Retrieve all privilege-based transfer rules")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Transfer rules retrieved successfully"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<List<TransferLimit>> getAllTransferRules() {
                SecurityUtils.checkStaffRole();
                log.info("Received request to get all transfer rules");

                List<TransferLimit> rules = transferLimitService.getAllTransferRules();
                return ResponseEntity.ok(rules);
        }

        @PostMapping("/check")
        @Operation(summary = "Check transfer feasibility", description = "Check if transfer amount is within limits")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Transfer feasibility checked successfully"),
                        @ApiResponse(responseCode = "404", description = "Account not found"),
                        @ApiResponse(responseCode = "422", description = "Validation error"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<Map<String, Object>> checkTransferFeasibility(
                        @Valid @RequestBody LimitCheckRequest request) {
                SecurityUtils.checkStaffRole();
                log.info("Received request to check transfer feasibility for account: {}, amount: {}",
                                request.getAccountNumber(), request.getAmount());

                boolean canTransfer = transferLimitService.checkTransferFeasibility(request);
                TransferLimitResponse limitInfo = transferLimitService.getRemainingLimit(request.getAccountNumber());

                Map<String, Object> response = Map.of(
                                "canTransfer", canTransfer,
                                "reason", canTransfer ? "Transfer is within limits" : "Transfer exceeds limits",
                                "remainingLimit", limitInfo.getDailyRemaining());

                return ResponseEntity.ok(response);
        }
}