package com.gdb.transactions.repository.mapper;

import com.gdb.transactions.domain.enums.PrivilegeLevel;
import com.gdb.transactions.domain.model.TransferLimit;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Row mapper for TransferLimit entity.
 */
@Component
public class TransferLimitRowMapper implements RowMapper<TransferLimit> {

    @Override
    public TransferLimit mapRow(ResultSet rs, int rowNum) throws SQLException {
        return TransferLimit.builder()
                .privilege(PrivilegeLevel.valueOf(rs.getString("privilege")))
                .dailyLimit(rs.getBigDecimal("daily_limit"))
                .perTransactionLimit(rs.getBigDecimal("per_transaction_limit"))
                .transactionLimit(rs.getInt("transaction_limit"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }
}