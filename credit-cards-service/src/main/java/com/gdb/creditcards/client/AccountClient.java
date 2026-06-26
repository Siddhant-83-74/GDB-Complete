package com.gdb.creditcards.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Client to the Account Service. Used by b.14 (mapping a credit card to an
 * account) to confirm the account exists and is active before linking.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AccountClient {

    private final RestTemplate restTemplate;

    @Value("${app.services.account-url:http://localhost:8001}")
    private String accountServiceUrl;

    /**
     * Returns true if the account exists. Fails closed (false) on any error so a
     * card is never linked to an unverifiable account.
     */
    public boolean accountExists(Long accountNumber) {
        String url = accountServiceUrl + "/api/v1/internal/accounts/" + accountNumber + "/exists";
        try {
            Boolean exists = restTemplate.getForObject(url, Boolean.class);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Error verifying account {} with Account Service: {}", accountNumber, e.getMessage());
            return false;
        }
    }
}
