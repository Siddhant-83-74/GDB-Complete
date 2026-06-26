package com.gdb.creditcards.repository.impl;

import com.gdb.creditcards.domain.model.CreditCardTransaction;
import com.gdb.creditcards.repository.CreditCardTransactionRepository;
import com.gdb.creditcards.repository.mapper.CreditCardTransactionRowMapper;
import com.gdb.creditcards.util.SqlLoader;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class CreditCardTransactionRepositoryImpl implements CreditCardTransactionRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CreditCardTransactionRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CreditCardTransaction save(CreditCardTransaction txn) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", txn.getId())
                .addValue("cardId", txn.getCardId())
                .addValue("type", txn.getType())
                .addValue("channel", txn.getChannel())
                .addValue("amount", txn.getAmount())
                .addValue("merchant", txn.getMerchant())
                .addValue("international", txn.isInternational())
                .addValue("status", txn.getStatus())
                .addValue("holdReason", txn.getHoldReason());
        jdbcTemplate.update(SqlLoader.get("SAVE_TRANSACTION"), params);
        return txn;
    }

    @Override
    public Optional<CreditCardTransaction> findById(String id) {
        List<CreditCardTransaction> results = jdbcTemplate.query(SqlLoader.get("FIND_TRANSACTION_BY_ID"),
                new MapSqlParameterSource("id", id), new CreditCardTransactionRowMapper());
        return results.stream().findFirst();
    }

    @Override
    public List<CreditCardTransaction> findByCardId(String cardId) {
        return jdbcTemplate.query(SqlLoader.get("FIND_TRANSACTIONS_BY_CARD"),
                new MapSqlParameterSource("cardId", cardId), new CreditCardTransactionRowMapper());
    }

    @Override
    public void updateStatus(String id, String status) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("status", status);
        jdbcTemplate.update(SqlLoader.get("UPDATE_TRANSACTION_STATUS"), params);
    }

    @Override
    public int countSince(String cardId, LocalDateTime since) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cardId", cardId)
                .addValue("since", since);
        Integer count = jdbcTemplate.queryForObject(SqlLoader.get("COUNT_TRANSACTIONS_SINCE"), params, Integer.class);
        return count == null ? 0 : count;
    }
}
