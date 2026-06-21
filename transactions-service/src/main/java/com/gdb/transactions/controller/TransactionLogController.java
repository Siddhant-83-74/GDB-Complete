package com.gdb.transactions.controller;

import com.gdb.transactions.domain.enums.TransactionType;
import com.gdb.transactions.dto.response.TransactionLogResponse;
import com.gdb.transactions.service.TransactionLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.gdb.transactions.security.SecurityUtils;

/**
 * REST controller for transaction log operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Transaction Logs", description = "Transaction log operations")
public class TransactionLogController {

        private final TransactionLogService transactionLogService;

        @GetMapping("/transaction-logs")
        @Operation(summary = "Get all transactions", description = "Retrieve all transactions across all accounts")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<Map<String, Object>> getAllTransactions(
                        @Parameter(description = "Maximum records to return") @RequestParam(defaultValue = "100") int limit,
                        @Parameter(description = "Pagination offset") @RequestParam(defaultValue = "0") int offset) {
                SecurityUtils.checkManagerRole();
                log.info("Received request to get all transactions with limit: {}, offset: {}", limit, offset);

                List<TransactionLogResponse> transactions = transactionLogService.getAllTransactionLogs(limit, offset);
                Long total = transactionLogService.getTotalCount();

                Map<String, Object> response = Map.of(
                                "logs", transactions,
                                "total", total,
                                "limit", limit,
                                "offset", offset);

                return ResponseEntity.ok(response);
        }

        @GetMapping({ "/transaction-logs/{accountNumber}", "/transactions/account/{accountNumber}" })
        @Operation(summary = "Get account transaction logs", description = "Retrieve transaction history for specific account")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Transaction logs retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Account not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<Map<String, Object>> getAccountTransactionLogs(
                        @Parameter(description = "Account number", required = true) @PathVariable Long accountNumber,
                        @Parameter(description = "Maximum records to return") @RequestParam(defaultValue = "50") int limit,
                        @Parameter(description = "Pagination offset") @RequestParam(defaultValue = "0") int offset,
                        @Parameter(description = "Filter by transaction type") @RequestParam(required = false) TransactionType transactionType,
                        @Parameter(description = "Filter from date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @Parameter(description = "Filter to date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
                SecurityUtils.checkManagerRole();
                log.info("Received request to get transaction logs for account: {}", accountNumber);

                List<TransactionLogResponse> transactions;

                if (transactionType != null) {
                        transactions = transactionLogService.getAccountTransactionLogsByType(accountNumber,
                                        transactionType, limit, offset);
                } else if (startDate != null && endDate != null) {
                        transactions = transactionLogService.getAccountTransactionLogsByDateRange(accountNumber,
                                        startDate, endDate, limit, offset);
                } else {
                        transactions = transactionLogService.getAccountTransactionLogs(accountNumber, limit, offset);
                }

                Long total = transactionLogService.getAccountTransactionCount(accountNumber);

                Map<String, Object> response = Map.of(
                                "accountNumber", accountNumber,
                                "logs", transactions,
                                "total", total);

                return ResponseEntity.ok(response);
        }

        @GetMapping({ "/transaction-logs/{accountNumber}/summary", "/transactions/account/{accountNumber}/summary" })
        @Operation(summary = "Get transaction summary", description = "Get aggregated transaction statistics")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Transaction summary retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Account not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<Map<String, Object>> getTransactionSummary(
                        @Parameter(description = "Account number", required = true) @PathVariable Long accountNumber,
                        @Parameter(description = "Summary from date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @Parameter(description = "Summary to date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
                SecurityUtils.checkManagerRole();
                log.info("Received request to get transaction summary for account: {}", accountNumber);

                // For now, return basic summary structure
                // This would typically involve more complex aggregation queries
                Map<String, Object> period = Map.of(
                                "startDate", startDate != null ? startDate : LocalDate.now().minusMonths(1),
                                "endDate", endDate != null ? endDate : LocalDate.now());

                Map<String, Object> summary = Map.of(
                                "totalWithdrawals", 0.0,
                                "totalDeposits", 0.0,
                                "totalTransfersSent", 0.0,
                                "totalTransfersReceived", 0.0,
                                "withdrawalCount", 0,
                                "depositCount", 0,
                                "transferCount", 0,
                                "netChange", 0.0);

                Map<String, Object> response = Map.of(
                                "accountNumber", accountNumber,
                                "period", period,
                                "summary", summary);

                return ResponseEntity.ok(response);
        }
}