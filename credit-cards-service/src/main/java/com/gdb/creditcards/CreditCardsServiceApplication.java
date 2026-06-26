package com.gdb.creditcards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Credit Cards Microservice.
 * Manages credit card lifecycle: application, transactions, bill payment,
 * service limits and card controls.
 *
 * The RestTemplate bean lives in {@link com.gdb.creditcards.config.RestTemplateConfig}.
 */
@SpringBootApplication
public class CreditCardsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditCardsServiceApplication.class, args);
    }
}
