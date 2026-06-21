package com.gdb.payment.service;

import com.gdb.payment.dto.PaymentRequest;
import com.gdb.payment.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

/**
 * Core Payment Gateway Service.
 * Simulates a real-world payment gateway (NPCI / SWIFT) with:
 * - Multi-mode support (NEFT, RTGS, IMPS, UPI)
 * - Transaction validation
 * - Simulated network latency (100-500ms)
 * - Random failure simulation (1% chance)
 * - Gateway reference ID generation
 */
@Service
@Slf4j
public class PaymentGatewayService {

    private static final double FAILURE_RATE = 0.01; // 1% random failure
    private static final int MIN_LATENCY_MS = 100;
    private static final int MAX_LATENCY_MS = 500;

    private final Random random = new Random();

    /**
     * Process a payment transaction through the gateway.
     *
     * Flow:
     * 1. Simulate network latency
     * 2. Validate amount (> 0)
     * 3. Validate accounts (source ≠ destination)
     * 4. Simulate random failure (1%)
     * 5. Generate gateway reference ID
     * 6. Return success/failure response
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        // Determine transaction_id: use reference_id if provided, else generate UUID
        String transactionId = (request.getReferenceId() != null && !request.getReferenceId().isBlank())
                ? request.getReferenceId()
                : UUID.randomUUID().toString();

        String gatewayRefId = UUID.randomUUID().toString();

        log.info("Processing payment: transactionId={}, mode={}, amount={}, source={}, destination={}",
                transactionId, request.getMode(), request.getAmount(),
                request.getSourceAccountId(), request.getDestinationAccountId());

        // 1. Simulate Network Latency (100-500ms)
        simulateNetworkLatency();

        // 2. Validate Amount (> 0)
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Payment FAILED: Invalid amount {}", request.getAmount());
            return PaymentResponse.builder()
                    .success(false)
                    .transactionId(transactionId)
                    .message("Invalid Amount: Must be greater than zero")
                    .gatewayRefId(gatewayRefId)
                    .build();
        }

        // 3. Validate Accounts (source ≠ destination)
        if (request.getSourceAccountId().equals(request.getDestinationAccountId())) {
            log.warn("Payment FAILED: Source and Destination are the same ({})", request.getSourceAccountId());
            return PaymentResponse.builder()
                    .success(false)
                    .transactionId(transactionId)
                    .message("Invalid Transaction: Source and Destination cannot be same")
                    .gatewayRefId(gatewayRefId)
                    .build();
        }

        // 4. Simulate Random Failure (1% chance)
        if (simulateRandomFailure()) {
            log.warn("Payment FAILED: Simulated network timeout for transactionId={}", transactionId);
            return PaymentResponse.builder()
                    .success(false)
                    .transactionId(transactionId)
                    .message("Gateway Error: Network Timeout")
                    .gatewayRefId(gatewayRefId)
                    .build();
        }

        // 5. Success
        log.info("Payment SUCCESSFUL: transactionId={}, gatewayRefId={}", transactionId, gatewayRefId);
        return PaymentResponse.builder()
                .success(true)
                .transactionId(transactionId)
                .message("Payment Processed Successfully")
                .gatewayRefId(gatewayRefId)
                .build();
    }

    /**
     * Simulates network latency between 100ms and 500ms.
     */
    private void simulateNetworkLatency() {
        int latency = MIN_LATENCY_MS + random.nextInt(MAX_LATENCY_MS - MIN_LATENCY_MS + 1);
        log.debug("Simulating network latency: {}ms", latency);
        try {
            Thread.sleep(latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Network latency simulation interrupted");
        }
    }

    /**
     * Simulates random network failure with 1% probability.
     *
     * @return true if failure should be simulated
     */
    private boolean simulateRandomFailure() {
        return random.nextDouble() < FAILURE_RATE;
    }
}
