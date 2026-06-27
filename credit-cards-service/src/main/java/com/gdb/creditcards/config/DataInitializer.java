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
 *
 * The dataset is intentionally large and varied — many transactions per card,
 * spread across ~10 months, with multiple types, channels and statuses — so the
 * pagination, date-range filtering and type filtering can all be demonstrated.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final CryptoUtil cryptoUtil;

    /** A realistic merchant with its usual channel and a typical spend band. */
    private record Merchant(String name, String channel, int minAmt, int maxAmt) {}

    private static final Merchant[] MERCHANTS = {
            // Shopping & electronics
            new Merchant("Amazon", "ECOMMERCE", 499, 18000),
            new Merchant("Flipkart", "ECOMMERCE", 399, 22000),
            new Merchant("Myntra", "ECOMMERCE", 799, 9000),
            new Merchant("Croma Electronics", "POS", 1500, 65000),
            new Merchant("Reliance Digital", "POS", 2000, 80000),
            new Merchant("DMart", "POS", 600, 9000),
            // Dining & food delivery
            new Merchant("Swiggy", "ECOMMERCE", 150, 2200),
            new Merchant("Zomato", "ECOMMERCE", 180, 2500),
            new Merchant("Starbucks", "POS", 250, 1200),
            new Merchant("Barbeque Nation", "POS", 1200, 6000),
            // Travel & fuel
            new Merchant("IRCTC Railways", "ECOMMERCE", 450, 4500),
            new Merchant("MakeMyTrip", "ECOMMERCE", 2500, 55000),
            new Merchant("Uber", "ECOMMERCE", 80, 1500),
            new Merchant("IndianOil Fuel", "POS", 500, 6000),
            new Merchant("IndiGo Airlines", "ECOMMERCE", 3500, 28000),
            // Utilities, health & entertainment
            new Merchant("Airtel Postpaid", "ECOMMERCE", 399, 1999),
            new Merchant("Tata Power", "ECOMMERCE", 800, 7000),
            new Merchant("Netflix", "ECOMMERCE", 199, 799),
            new Merchant("BookMyShow", "ECOMMERCE", 250, 2000),
            new Merchant("Apollo Pharmacy", "POS", 200, 4500),
    };

    @Override
    public void run(String... args) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM credit_cards", Integer.class);
        if (count != null && count > 0) {
            return;
        }
        log.info("No credit cards found. Seeding demo cards and transactions...");

        // userId, holder, mobile, vendor, category, creditLimit, outstanding, txnCount, status
        seedCard(1L, "John Doe", "9876543210", "VISA", "PLATINUM",
                new BigDecimal("1000000"), new BigDecimal("375000"), 34);
        seedCard(1L, "John Doe", "9876543210", "MASTERCARD", "GOLD",
                new BigDecimal("300000"), new BigDecimal("42000"), 28);
        seedCard(1L, "John Doe", "9876543210", "RUPAY", "SILVER",
                new BigDecimal("150000"), new BigDecimal("18000"), 22);
        seedCard(2L, "Asha Rao", "9988776655", "RUPAY", "GOLD",
                new BigDecimal("300000"), new BigDecimal("120000"), 31);
        seedCard(2L, "Asha Rao", "9988776655", "VISA", "PLATINUM",
                new BigDecimal("800000"), new BigDecimal("260000"), 40);
        seedCard(3L, "System Admin", "9000000000", "VISA", "SILVER",
                new BigDecimal("100000"), new BigDecimal("9500"), 17);
        // Extra holders so the portfolio, selector search and analytics show a fuller book.
        seedCard(2L, "Priya Menon", "9812345670", "MASTERCARD", "PLATINUM",
                new BigDecimal("900000"), new BigDecimal("415000"), 37);
        seedCard(1L, "Rahul Verma", "9700011122", "VISA", "GOLD",
                new BigDecimal("400000"), new BigDecimal("88000"), 26);
        seedCard(3L, "Neha Gupta", "9898989898", "RUPAY", "PLATINUM",
                new BigDecimal("750000"), new BigDecimal("210000"), 33);
        seedCard(2L, "Vikram Singh", "9676543210", "VISA", "GOLD",
                new BigDecimal("350000"), new BigDecimal("47500"), 24);
        seedCard(1L, "Sana Khan", "9555012345", "MASTERCARD", "SILVER",
                new BigDecimal("120000"), new BigDecimal("13200"), 19);
        // A blocked and an inactive card to exercise the status filter.
        seedCard(3L, "System Admin", "9000000000", "MASTERCARD", "PLATINUM",
                new BigDecimal("1200000"), new BigDecimal("540000"), 29,
                CreditCardConstants.STATUS_BLOCKED);
        seedCard(2L, "Asha Rao", "9988776655", "MASTERCARD", "GOLD",
                new BigDecimal("250000"), new BigDecimal("0"), 12,
                CreditCardConstants.STATUS_INACTIVE);

        log.info("Demo credit cards and transactions seeded successfully.");
    }

    private void seedCard(Long userId, String name, String mobile, String vendor, String category,
            BigDecimal creditLimit, BigDecimal outstanding, int txnCount) {
        seedCard(userId, name, mobile, vendor, category, creditLimit, outstanding, txnCount,
                CreditCardConstants.STATUS_ACTIVE);
    }

    private void seedCard(Long userId, String name, String mobile, String vendor, String category,
            BigDecimal creditLimit, BigDecimal outstanding, int txnCount, String status) {
        String id = UUID.randomUUID().toString();
        String pan = CardNumberGenerator.generate(vendor);
        BigDecimal available = creditLimit.subtract(outstanding);

        String[] leadSources = {"BRANCH", "COLD_CALL", "DIGITAL_CAMPAIGN"};
        String leadSource = leadSources[ThreadLocalRandom.current().nextInt(leadSources.length)];
        String branchCode = String.format("BR-DEL-%03d", ThreadLocalRandom.current().nextInt(1, 100));

        jdbcTemplate.update("""
                INSERT INTO credit_cards (
                    id, user_id, card_holder_name, mobile_number,
                    card_number_encrypted, card_bin, card_last4, vendor, category,
                    cvv_hash, expiry_date, credit_limit, available_credit, outstanding_amount,
                    international_enabled, status,
                    consent_source, otp_verified, lead_source, sourcing_branch_code,
                    kyc_document_name, income_document_name)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """,
                id, userId, name, mobile,
                cryptoUtil.encrypt(pan), pan.substring(0, 6), pan.substring(pan.length() - 4), vendor, category,
                BCrypt.hashpw(String.format("%03d", ThreadLocalRandom.current().nextInt(1000)), BCrypt.gensalt()),
                LocalDate.now().plusYears(4), creditLimit, available, outstanding,
                false, status,
                "PHYSICAL_FORM,DIGITAL_SIGNATURE", true, leadSource, branchCode,
                "kyc_" + name.toLowerCase().replace(" ", "_") + ".pdf",
                "income_" + name.toLowerCase().replace(" ", "_") + ".pdf");

        seedTransactions(id, txnCount);
    }

    /**
     * Generates a realistic transaction history of {@code count} entries spread
     * backwards over roughly ten months, mixing purchases, bill payments and
     * refunds across several channels and statuses.
     */
    private void seedTransactions(String cardId, int count) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        LocalDateTime when = LocalDateTime.now().minusHours(rnd.nextInt(1, 18));

        for (int i = 0; i < count; i++) {
            // Step backwards by a few days each time so history spans many months.
            when = when.minusDays(rnd.nextInt(2, 11)).minusHours(rnd.nextInt(0, 24));

            int roll = rnd.nextInt(100);
            if (roll < 12) {
                // Monthly-style bill payment (credits the card).
                BigDecimal amt = BigDecimal.valueOf(rnd.nextInt(2000, 40000));
                insertTransaction(cardId, CreditCardConstants.TXN_PAYMENT, "NETBANKING",
                        amt, "Credit Card Bill Payment", CreditCardConstants.TXN_APPROVED, false, when);
            } else if (roll < 20) {
                // Refund from a prior purchase.
                Merchant m = MERCHANTS[rnd.nextInt(MERCHANTS.length)];
                BigDecimal amt = BigDecimal.valueOf(rnd.nextInt(m.minAmt(), m.maxAmt()));
                insertTransaction(cardId, CreditCardConstants.TXN_REFUND, m.channel(),
                        amt, m.name() + " Refund", CreditCardConstants.TXN_APPROVED, false, when);
            } else {
                // Regular purchase, occasionally international, with a mix of statuses.
                Merchant m = MERCHANTS[rnd.nextInt(MERCHANTS.length)];
                boolean intl = rnd.nextInt(100) < 8;
                int amt = rnd.nextInt(m.minAmt(), m.maxAmt());
                if (intl) {
                    amt *= 2; // international spends tend to be larger
                }
                String status = CreditCardConstants.TXN_APPROVED;
                int s = rnd.nextInt(100);
                if (s < 6) {
                    status = CreditCardConstants.TXN_DECLINED;
                } else if (s < 14) {
                    status = CreditCardConstants.TXN_ON_HOLD;
                } else if (s < 17) {
                    status = CreditCardConstants.TXN_REVERSED;
                }
                String channel = intl ? "INTERNATIONAL" : m.channel();
                insertTransaction(cardId, CreditCardConstants.TXN_PURCHASE, channel,
                        BigDecimal.valueOf(amt), m.name(), status, intl, when);
            }
        }
    }

    private void insertTransaction(String cardId, String type, String channel,
            BigDecimal amount, String merchant, String status, boolean international, LocalDateTime createdAt) {
        jdbcTemplate.update("""
                INSERT INTO credit_card_transactions (
                    id, card_id, type, channel, amount, merchant, international, status, created_at)
                VALUES (?,?,?,?,?,?,?,?,?)
                """,
                UUID.randomUUID().toString(), cardId, type, channel, amount, merchant,
                international, status, Timestamp.valueOf(createdAt));
    }
}
