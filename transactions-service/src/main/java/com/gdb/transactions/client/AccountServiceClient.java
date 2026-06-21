package com.gdb.transactions.client;

import com.gdb.transactions.client.dto.AccountResponse;
import com.gdb.transactions.client.dto.PinVerificationRequest;
import com.gdb.transactions.client.dto.PinVerificationResponse;
import com.gdb.transactions.client.dto.AccountOperationRequest;
import com.gdb.transactions.client.dto.AccountOperationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdb.transactions.domain.enums.PrivilegeLevel;
import com.gdb.transactions.exception.TransactionException;
import com.gdb.transactions.exception.custom.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Client for communicating with the Account Service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountServiceClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${external.account-service.url}")
    private String accountServiceUrl;

    @Value("${external.account-service.timeout:10000}")
    private int timeout;

    @Value("${external.account-service.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${external.account-service.retry.delay:1000}")
    private int retryDelay;

    /**
     * Validate account exists and is active.
     */
    public AccountResponse validateAccount(Long accountNumber) {
        String url = accountServiceUrl + "/api/v1/internal/accounts/" + accountNumber;

        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(AccountResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .retryWhen(Retry.fixedDelay(maxRetryAttempts, Duration.ofMillis(retryDelay)))
                    .block();
        } catch (WebClientResponseException e) {
            handleWebClientException(e, "validate account");
            return null;
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Account service is unreachable");
        } catch (Exception e) {
            log.error("Error validating account {}: {}", accountNumber, e.getMessage());
            throw new TransactionException("ACCOUNT_SERVICE_ERROR", "Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Verify PIN for account.
     */
    public boolean verifyPin(Long accountNumber, String pin) {
        String url = accountServiceUrl + "/api/v1/internal/accounts/" + accountNumber + "/verify-pin";

        java.util.Map<String, String> request = java.util.Map.of("pin", pin);

        log.debug("Calling PIN verification: URL={}, AccountNumber={}, PIN={}", url, accountNumber, pin);

        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Boolean> response = webClient.post()
                    .uri(url)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(java.util.Map.class)
                    .timeout(Duration.ofMillis(timeout))
                    .retryWhen(Retry.fixedDelay(maxRetryAttempts, Duration.ofMillis(retryDelay)))
                    .block();

            log.debug("PIN verification response: {}", response);
            boolean result = response != null && Boolean.TRUE.equals(response.get("valid"));
            log.debug("PIN verification result: {}", result);
            return result;
        } catch (WebClientResponseException e) {
            log.error("WebClient response exception during PIN verification: status={}, body={}", e.getStatusCode(),
                    e.getResponseBodyAsString());
            handleWebClientException(e, "verify PIN");
            return false; // Never reached due to exception
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Account service is unreachable");
        } catch (Exception e) {
            log.error("Generic exception during PIN verification for account {}: {}", accountNumber, e.getMessage(), e);
            throw new TransactionException("ACCOUNT_SERVICE_ERROR", "Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Debit amount from account.
     */
    public AccountOperationResponse debitAccount(Long accountNumber, BigDecimal amount, String description) {
        String url = accountServiceUrl + "/api/v1/internal/accounts/debit";

        AccountOperationRequest request = AccountOperationRequest.builder()
                .accountNumber(accountNumber)
                .amount(amount)
                .description(description)
                .build();

        try {
            return webClient.post()
                    .uri(url)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AccountOperationResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .retryWhen(Retry.fixedDelay(maxRetryAttempts, Duration.ofMillis(retryDelay)))
                    .block();
        } catch (WebClientResponseException e) {
            handleWebClientException(e, "debit account");
            return null;
        } catch (WebClientRequestException e) {
            log.error("Account service connection failed during {}: {}", "debit account", e.getMessage());
            throw new ServiceUnavailableException("Account service is unreachable");
        } catch (Exception e) {
            log.error("Error debiting account {}: {}", accountNumber, e.getMessage());
            throw new TransactionException("ACCOUNT_SERVICE_ERROR", "Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Credit amount to account.
     */
    public AccountOperationResponse creditAccount(Long accountNumber, BigDecimal amount, String description) {
        String url = accountServiceUrl + "/api/v1/internal/accounts/credit";

        AccountOperationRequest request = AccountOperationRequest.builder()
                .accountNumber(accountNumber)
                .amount(amount)
                .description(description)
                .build();

        try {
            return webClient.post()
                    .uri(url)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AccountOperationResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .retryWhen(Retry.fixedDelay(maxRetryAttempts, Duration.ofMillis(retryDelay)))
                    .block();
        } catch (WebClientResponseException e) {
            handleWebClientException(e, "credit account");
            return null;
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Account service is unreachable");
        } catch (Exception e) {
            log.error("Error crediting account {}: {}", accountNumber, e.getMessage());
            throw new TransactionException("ACCOUNT_SERVICE_ERROR", "Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Get account privilege level.
     */
    public PrivilegeLevel getAccountPrivilege(Long accountNumber) {
        String url = accountServiceUrl + "/api/v1/internal/accounts/" + accountNumber + "/privilege";

        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, String> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(java.util.Map.class)
                    .timeout(Duration.ofMillis(timeout))
                    .retryWhen(Retry.fixedDelay(maxRetryAttempts, Duration.ofMillis(retryDelay)))
                    .block();

            String privilegeStr = response != null ? response.get("privilege") : null;
            return privilegeStr != null ? PrivilegeLevel.valueOf(privilegeStr) : null;
        } catch (WebClientResponseException e) {
            handleWebClientException(e, "get account privilege");
            return null;
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Account service is unreachable");
        } catch (Exception e) {
            log.error("Error getting privilege for account {}: {}", accountNumber, e.getMessage());
            throw new TransactionException("ACCOUNT_SERVICE_ERROR", "Unexpected error: " + e.getMessage());
        }
    }

    private void handleWebClientException(WebClientResponseException e, String operation) {
        HttpStatus status = (HttpStatus) e.getStatusCode();
        String responseBody = e.getResponseBodyAsString();
        String errorCode = "ACCOUNT_SERVICE_ERROR";
        String errorMessage = "Account service error during " + operation;

        // Try to parse error details from response body
        if (responseBody != null && !responseBody.isEmpty()) {
            try {
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                if (jsonNode.has("errorCode")) {
                    errorCode = jsonNode.get("errorCode").asText();
                }
                if (jsonNode.has("message")) {
                    errorMessage = jsonNode.get("message").asText();
                }
            } catch (Exception parseEx) {
                log.warn("Failed to parse error response body: {}", parseEx.getMessage());
            }
        }

        log.error("Account service error during {}: {} - {} - {}", operation, status, errorCode, errorMessage);

        switch (status) {
            case NOT_FOUND:
                throw new AccountNotFoundException(errorMessage);
            case UNPROCESSABLE_ENTITY:
                if ("ACCOUNT_NOT_ACTIVE".equals(errorCode) || "ACCOUNT_INACTIVE".equals(errorCode)) {
                    throw new AccountNotActiveException(errorMessage);
                }
                throw new TransactionException(errorCode, errorMessage);
            case BAD_REQUEST:
                if ("INSUFFICIENT_FUNDS".equals(errorCode) || "INSUFFICIENT_BALANCE".equals(errorCode)) {
                    throw new InsufficientFundsException(errorMessage);
                }
                throw new TransactionException(errorCode, errorMessage);
            case UNAUTHORIZED:
                throw new InvalidPinException(errorMessage);
            case SERVICE_UNAVAILABLE:
                throw new ServiceUnavailableException("Account service is currently unavailable");
            default:
                throw new TransactionException(errorCode, errorMessage);
        }
    }
}