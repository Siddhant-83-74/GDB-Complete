package com.gdb.transactions.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        seedTransactionLogs();
    }

    private void seedTransactionLogs() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transaction_logging", Integer.class);
        if (count == null || count == 0) {
            log.info("No transaction logs found in DB. Seeding dummy logs...");

            // Deposit to 1001 (John Doe)
            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, 1001L, new BigDecimal("60000.00"), "DEPOSIT", null, "Initial deposit", "CASH", "SUCCESS");

            // Transfer from 1002 (Admin) to 1001 (John Doe) of 10000
            jdbcTemplate.update("""
                INSERT INTO fund_transfers (from_account, to_account, transfer_amount, transfer_mode)
                VALUES (?, ?, ?, ?)
                """, 1002L, 1001L, new BigDecimal("10000.00"), "IMPS");

            // Debit log for 1002
            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, 1002L, new BigDecimal("10000.00"), "TRANSFER", 1L, "Fund Transfer to 1001", "IMPS", "SUCCESS");

            // Credit log for 1001
            jdbcTemplate.update("""
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, 1001L, new BigDecimal("10000.00"), "TRANSFER", 1L, "Fund Transfer from 1002", "IMPS", "SUCCESS");

            log.info("Default transaction logs seeded successfully.");
        }
    }
}
