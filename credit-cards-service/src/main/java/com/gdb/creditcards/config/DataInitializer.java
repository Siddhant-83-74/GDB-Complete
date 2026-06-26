package com.gdb.creditcards.config;

import com.gdb.creditcards.constants.CreditCardConstants;
import com.gdb.creditcards.util.CardNumberGenerator;
import com.gdb.creditcards.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Seeds demo credit cards and transactions on first startup so the UI shows
 * real data. Runs only when the table is empty. Cards are seeded for user ids
 * 1, 2 and 3 (common demo logins) so whoever signs in sees their own cards.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final CryptoUtil cryptoUtil;

    private static final String[] PURCHASE_MERCHANTS = {
            "Amazon", "Flipkart", "Swiggy", "Zomato", "Uber", "Myntra", "BookMyShow", "IRCTC"
    };

    @Override
    public void run(String... args) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM credit_cards", Integer.class);
        if (count != null && count > 0) {
            return;
        }
        log.info("No credit cards found. Seeding demo cards and transactions...");

        seedCard(1L, "John Doe", "9876543210", "VISA", "PLATINUM",
                new BigDecimal("1000000"), new BigDecimal("375000"), 8);
        seedCard(1L, "John Doe", "9876543210", "MASTERCARD", "GOLD",
                new BigDecimal("300000"), new BigDecimal("42000"), 5);
        seedCard(2L, "Asha Rao", "9988776655", "RUPAY", "GOLD",
                new BigDecimal("300000"), new BigDecimal("120000"), 6);
        seedCard(3L, "System Admin", "9000000000", "VISA", "SILVER",
                new BigDecimal("100000"), new BigDecimal("9500"), 4);

        log.info("Demo credit cards seeded successfully.");
    }

    private void seedCard(Long userId, String name, String mobile, String vendor, String category,
            BigDecimal creditLimit, BigDecimal outstanding, int txnCount) {
        String id = UUID.randomUUID().toString();
        String pan = CardNumberGenerator.generate(vendor);
        BigDecimal available = creditLimit.subtract(outstanding);

        jdbcTemplate.update("""
                INSERT INTO credit_cards (
                    id, user_id, card_holder_name, mobile_number,
                    card_number_encrypted, card_bin, card_last4, vendor, category,
                    cvv_hash, expiry_date, credit_limit, available_credit, outstanding_amount,
                    international_enabled, status)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """,
                id, userId, name, mobile,
                cryptoUtil.encrypt(pan), pan.substring(0, 6), pan.substring(pan.length() - 4), vendor, category,
                BCrypt.hashpw(String.format("%03d", ThreadLocalRandom.current().nextInt(1000)), BCrypt.gensalt()),
                LocalDate.now().plusYears(4), creditLimit, available, outstanding,
                false, CreditCardConstants.STATUS_ACTIVE);

        seedTransactions(id, outstanding, txnCount);
    }

    private void seedTransactions(String cardId, BigDecimal outstanding, int count) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        LocalDateTime when = LocalDateTime.now();

        // A bill payment first (most recent), then spread purchases backwards in time.
        insertTransaction(cardId, CreditCardConstants.TXN_PAYMENT, "ECOMMERCE",
                new BigDecimal("15000"), "Credit Card Bill Payment", when.minusDays(2));

        for (int i = 0; i < count; i++) {
            when = when.minusDays(rnd.nextInt(1, 6));
            BigDecimal amount = BigDecimal.valueOf(rnd.nextInt(500, 12000));
            String merchant = PURCHASE_MERCHANTS[rnd.nextInt(PURCHASE_MERCHANTS.length)];
            insertTransaction(cardId, CreditCardConstants.TXN_PURCHASE, "POS", amount, merchant, when);
        }
    }

    private void insertTransaction(String cardId, String type, String channel,
            BigDecimal amount, String merchant, LocalDateTime createdAt) {
        jdbcTemplate.update("""
                INSERT INTO credit_card_transactions (
                    id, card_id, type, channel, amount, merchant, international, status, created_at)
                VALUES (?,?,?,?,?,?,?,?,?)
                """,
                UUID.randomUUID().toString(), cardId, type, channel, amount, merchant,
                false, CreditCardConstants.TXN_APPROVED, Timestamp.valueOf(createdAt));
    }
}
