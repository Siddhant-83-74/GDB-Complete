package com.gdb.creditcards.repository.mapper;

import com.gdb.creditcards.domain.model.CreditCardTransaction;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CreditCardTransactionRowMapper implements RowMapper<CreditCardTransaction> {

    @Override
    public CreditCardTransaction mapRow(ResultSet rs, int rowNum) throws SQLException {
        return CreditCardTransaction.builder()
                .id(rs.getString("id"))
                .cardId(rs.getString("card_id"))
                .type(rs.getString("type"))
                .channel(rs.getString("channel"))
                .amount(rs.getBigDecimal("amount"))
                .merchant(rs.getString("merchant"))
                .international(rs.getBoolean("international"))
                .status(rs.getString("status"))
                .holdReason(rs.getString("hold_reason"))
                .createdAt(rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime()
                        : null)
                .build();
    }
}
