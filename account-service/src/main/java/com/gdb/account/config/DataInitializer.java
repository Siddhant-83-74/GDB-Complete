package com.gdb.account.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
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
        seedAccounts();
    }

    private void seedAccounts() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM accounts", Integer.class);
        if (count == null || count == 0) {
            log.info("No accounts found in DB. Seeding default accounts...");

            String pinHash = BCrypt.hashpw("1234", BCrypt.gensalt(12));

            // Seed Savings Account (John Doe)
            jdbcTemplate.update("""
                INSERT INTO accounts (account_number, account_type, name, pin_hash, balance, privilege, bank_name, bank_branch, ifsc_code, is_active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, 1001L, "SAVINGS", "John Doe", pinHash, new BigDecimal("50000.00"), "GOLD", "Global Digital Bank", "Main Branch", "GDB0000001", true);

            jdbcTemplate.update("""
                INSERT INTO savings_account_details (account_number, date_of_birth, gender, phone_no, aadhar_number)
                VALUES (?, ?::date, ?::gender_enum, ?, ?)
                """, 1001L, "1990-01-01", "Male", "9876543210", "123456789012");

            // Seed Current Account (Admin)
            jdbcTemplate.update("""
                INSERT INTO accounts (account_number, account_type, name, pin_hash, balance, privilege, bank_name, bank_branch, ifsc_code, is_active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, 1002L, "CURRENT", "System Admin", pinHash, new BigDecimal("250000.00"), "PREMIUM", "Global Digital Bank", "Main Branch", "GDB0000001", true);

            jdbcTemplate.update("""
                INSERT INTO current_account_details (account_number, company_name, website, registration_no)
                VALUES (?, ?, ?, ?)
                """, 1002L, "Admin Tech Corp", "admintech.com", "U12345MH2020PTC123456");

            // Update sequence value so next generated number doesn't conflict
            jdbcTemplate.execute("SELECT setval('account_number_seq', 1005)");

            log.info("Default accounts seeded successfully.");
        }
    }
}
