package com.gdb.creditcards.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Client to the Payment Gateway Service, used when a customer pays a credit
 * card bill from an external/source instrument.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentGatewayClient {

    private final RestTemplate restTemplate;

    @Value("${app.services.payment-gateway-url:http://localhost:8008}")
    private String paymentGatewayUrl;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentResult {
        private boolean success;
        private String reference;
        private String message;
    }

    public PaymentResult processPayment(String cardId, BigDecimal amount, String sourceReference) {
        String url = paymentGatewayUrl + "/api/v1/payment/process";
        try {
            PaymentResult result = restTemplate.postForObject(url,
                    Map.of("cardId", cardId, "amount", amount, "source", sourceReference == null ? "" : sourceReference),
                    PaymentResult.class);
            if (result != null) {
                return result;
            }
        } catch (Exception e) {
            log.error("Payment gateway error for card {}: {}", cardId, e.getMessage());
        }
        // Fail closed - the caller decides how to surface a declined payment.
        return PaymentResult.builder().success(false).message("Payment gateway unavailable").build();
    }
}
