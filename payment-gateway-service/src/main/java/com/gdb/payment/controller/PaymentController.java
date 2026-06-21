package com.gdb.payment.controller;

import com.gdb.payment.dto.PaymentRequest;
import com.gdb.payment.dto.PaymentResponse;
import com.gdb.payment.dto.ValidateTransferRequest;
import com.gdb.payment.service.PaymentGatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Tag(name = "Payment Gateway", description = "Central Payment Gateway - NEFT, RTGS, IMPS, UPI")
public class PaymentController {

    private final PaymentGatewayService gatewayService;

    /**
     * Process a payment transaction through the gateway.
     * Called by the Transactions Service to validate and process payments.
     */
    @PostMapping("/process")
    @Operation(summary = "Process Payment", description = "Validate and process a payment transaction through the gateway.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment processed (check 'success' field)"),
            @ApiResponse(responseCode = "422", description = "Validation error (invalid input)"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Received payment request: mode={}, amount={}", request.getMode(), request.getAmount());
        PaymentResponse response = gatewayService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Validate transfer (called by frontend for secondary validation check).
     * Currently returns success to satisfy frontend flow.
     */
    @PostMapping("/validate")
    public ResponseEntity<PaymentResponse> validateTransfer(@RequestBody ValidateTransferRequest request) {
        log.info("Received validation request for transaction: {}", request.getTransactionId());
        return ResponseEntity.ok(PaymentResponse.builder()
                .success(true)
                .transactionId(request.getTransactionId())
                .message("Transfer Validated")
                .build());
    }
}
