package com.gdb.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main entry point for the Account Microservice.
 * This service manages bank accounts, balances, and integrations.
 *
 * The RestTemplate bean lives in {@link com.gdb.account.config.RestTemplateConfig}.
 */
@SpringBootApplication
@EnableCaching
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
