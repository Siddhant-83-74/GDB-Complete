package com.gdb.creditcards.repository.impl;

import com.gdb.creditcards.domain.model.CardServiceLimit;
import com.gdb.creditcards.domain.model.CreditCard;
import com.gdb.creditcards.repository.CreditCardRepository;
import com.gdb.creditcards.repository.mapper.CardServiceLimitRowMapper;
import com.gdb.creditcards.repository.mapper.CreditCardRowMapper;
import com.gdb.creditcards.util.SqlLoader;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class CreditCardRepositoryImpl implements CreditCardRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CreditCardRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CreditCard save(CreditCard card) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", card.getId())
                .addValue("userId", card.getUserId())
                .addValue("cardHolderName", card.getCardHolderName())
                .addValue("mobileNumber", card.getMobileNumber())
                .addValue("cardNumberEncrypted", card.getCardNumberEncrypted())
                .addValue("cardBin", card.getCardBin())
                .addValue("cardLast4", card.getCardLast4())
                .addValue("vendor", card.getVendor())
                .addValue("category", card.getCategory())
                .addValue("cvvHash", card.getCvvHash())
                .addValue("expiryDate", card.getExpiryDate())
                .addValue("creditLimit", card.getCreditLimit())
                .addValue("availableCredit", card.getAvailableCredit())
                .addValue("outstandingAmount", card.getOutstandingAmount())
                .addValue("linkedAccountNumber", card.getLinkedAccountNumber())
                .addValue("internationalEnabled", card.getInternationalEnabled())
                .addValue("status", card.getStatus())
                .addValue("consentSource", card.getConsentSource())
                .addValue("otpVerified", card.getOtpVerified() != null && card.getOtpVerified())
                .addValue("leadSource", card.getLeadSource())
                .addValue("sourcingBranchCode", card.getSourcingBranchCode())
                .addValue("kycDocumentName", card.getKycDocumentName())
                .addValue("incomeDocumentName", card.getIncomeDocumentName());
        jdbcTemplate.update(SqlLoader.get("SAVE_CARD"), params);
        return card;
    }

    @Override
    public Optional<CreditCard> findById(String id) {
        List<CreditCard> results = jdbcTemplate.query(SqlLoader.get("FIND_CARD_BY_ID"),
                new MapSqlParameterSource("id", id), new CreditCardRowMapper());
        return results.stream().findFirst();
    }

    @Override
    public List<CreditCard> findByUserId(Long userId) {
        return jdbcTemplate.query(SqlLoader.get("FIND_CARDS_BY_USER"),
                new MapSqlParameterSource("userId", userId), new CreditCardRowMapper());
    }

    @Override
    public void updateBalances(String id, BigDecimal availableCredit, BigDecimal outstandingAmount) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("availableCredit", availableCredit)
                .addValue("outstandingAmount", outstandingAmount);
        jdbcTemplate.update(SqlLoader.get("UPDATE_CARD_BALANCES"), params);
    }

    @Override
    public void updateStatus(String id, String status) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("status", status);
        jdbcTemplate.update(SqlLoader.get("UPDATE_CARD_STATUS"), params);
    }

    @Override
    public void updateInternational(String id, boolean enabled) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("enabled", enabled);
        jdbcTemplate.update(SqlLoader.get("UPDATE_CARD_INTERNATIONAL"), params);
    }

    @Override
    public void linkAccount(String id, Long accountNumber) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("accountNumber", accountNumber);
        jdbcTemplate.update(SqlLoader.get("LINK_CARD_ACCOUNT"), params);
    }

    @Override
    public void upsertServiceLimit(CardServiceLimit limit) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", limit.getId())
                .addValue("cardId", limit.getCardId())
                .addValue("channel", limit.getChannel())
                .addValue("perTxnLimit", limit.getPerTxnLimit());
        jdbcTemplate.update(SqlLoader.get("UPSERT_SERVICE_LIMIT"), params);
    }

    @Override
    public List<CardServiceLimit> findServiceLimits(String cardId) {
        return jdbcTemplate.query(SqlLoader.get("FIND_SERVICE_LIMITS"),
                new MapSqlParameterSource("cardId", cardId), new CardServiceLimitRowMapper());
    }

    @Override
    public Optional<CardServiceLimit> findServiceLimit(String cardId, String channel) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cardId", cardId)
                .addValue("channel", channel);
        List<CardServiceLimit> results = jdbcTemplate.query(SqlLoader.get("FIND_SERVICE_LIMIT_BY_CHANNEL"),
                params, new CardServiceLimitRowMapper());
        return results.stream().findFirst();
    }
}
