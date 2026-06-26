package com.gdb.creditcards.repository.mapper;

import com.gdb.creditcards.domain.model.CardServiceLimit;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CardServiceLimitRowMapper implements RowMapper<CardServiceLimit> {

    @Override
    public CardServiceLimit mapRow(ResultSet rs, int rowNum) throws SQLException {
        return CardServiceLimit.builder()
                .id(rs.getString("id"))
                .cardId(rs.getString("card_id"))
                .channel(rs.getString("channel"))
                .perTxnLimit(rs.getBigDecimal("per_txn_limit"))
                .build();
    }
}
