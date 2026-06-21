package com.gdb.transactions.client;

import com.gdb.transactions.client.dto.PaymentGatewayResponse;
import com.gdb.transactions.exception.TransactionException;
import com.gdb.transactions.exception.custom.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for communicating with the Central Payment Gateway Service.
 * Acts as a second-level validation layer before processing transactions.
 *
 * The gateway performs:
 * - Amount validation (> 0)
 * - Account validation (source ≠ destination)
 * - Simulated network latency (100-500ms)
 * - Random failure simulation (1% chance)
 * - Gateway reference ID generation
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentGatewayClient {

    private final WebClient webClient;

    @Value("${external.payment-gateway-service.url}")
    private String paymentGatewayUrl;

    @Value("${external.payment-gateway-service.timeout:10000}")
    private int timeout;

    /**
     * Process a payment through the Central Payment Gateway.
     *
     * @param sourceAccountId      Source account number
     * @param destinationAccountId Destination account number
     * @param amount               Transfer amount
     * @param mode                 Transfer mode (NEFT, RTGS, IMPS, UPI)
     * @param referenceId          Optional reference ID for idempotency
     * @return PaymentGatewayResponse with success/failure and gateway_ref_id
     */
    public PaymentGatewayResponse processPayment(Long sourceAccountId,
            Long destinationAccountId,
            BigDecimal amount,
            String mode,
            String referenceId) {

        String url = paymentGatewayUrl + "/api/v1/payment/process";
        log.info("Calling Payment Gateway: url={}, source={}, dest={}, amount={}, mode={}",
                url, sourceAccountId, destinationAccountId, amount, mode);

        // Build request body with snake_case keys (matching gateway expectations)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("source_account_id", sourceAccountId);
        requestBody.put("destination_account_id", destinationAccountId);
        requestBody.put("amount", amount);
        requestBody.put("mode", mode);
        if (referenceId != null && !referenceId.isBlank()) {
            requestBody.put("reference_id", referenceId);
        }

        try {
            PaymentGatewayResponse response = webClient.post()
                    .uri(url)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(PaymentGatewayResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            if (response != null) {
                log.info("Payment Gateway response: success={}, message={}, gatewayRefId={}",
                        response.isSuccess(), response.getMessage(), response.getGatewayRefId());
            } else {
                log.warn("Received null response from Payment Gateway");
            }

            return response;

        } catch (WebClientResponseException e) {
            log.error("Payment Gateway returned error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new TransactionException("PAYMENT_GATEWAY_ERROR",
                    "Payment gateway error: " + e.getResponseBodyAsString());
        } catch (WebClientRequestException e) {
            log.error("Payment Gateway is unreachable: {}", e.getMessage());
            throw new ServiceUnavailableException("Payment Gateway Service is unreachable");
        } catch (Exception e) {
            log.error("Unexpected error calling Payment Gateway: {}", e.getMessage(), e);
            throw new TransactionException("PAYMENT_GATEWAY_ERROR",
                    "Unexpected error communicating with payment gateway: " + e.getMessage());
        }
    }
}
