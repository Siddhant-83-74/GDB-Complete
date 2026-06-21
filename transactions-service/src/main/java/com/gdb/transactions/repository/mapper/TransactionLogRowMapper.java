package com.gdb.transactions.repository.mapper;

import com.gdb.transactions.domain.enums.TransactionType;
import com.gdb.transactions.domain.model.TransactionLog;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Row mapper for TransactionLog entity.
 */
@Component
public class TransactionLogRowMapper implements RowMapper<TransactionLog> {

    @Override
    public TransactionLog mapRow(ResultSet rs, int rowNum) throws SQLException {
        return TransactionLog.builder()
                .id(rs.getLong("id"))
                .accountNumber(rs.getLong("account_number"))
                .amount(rs.getBigDecimal("amount"))
                .transactionType(TransactionType.valueOf(rs.getString("transaction_type")))
                .referenceId(rs.getObject("reference_id", Long.class))
                .description(rs.getString("description"))
                .mode(rs.getString("mode"))
                .status(rs.getString("status"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }
}