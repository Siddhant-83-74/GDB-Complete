package com.gdb.transactions.repository.mapper;

import com.gdb.transactions.domain.enums.TransferMode;
import com.gdb.transactions.domain.model.FundTransfer;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Row mapper for FundTransfer entity.
 */
@Component
public class FundTransferRowMapper implements RowMapper<FundTransfer> {

    @Override
    public FundTransfer mapRow(ResultSet rs, int rowNum) throws SQLException {
        return FundTransfer.builder()
                .id(rs.getLong("id"))
                .fromAccount(rs.getLong("from_account"))
                .toAccount(rs.getLong("to_account"))
                .transferAmount(rs.getBigDecimal("transfer_amount"))
                .transferMode(TransferMode.valueOf(rs.getString("transfer_mode")))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }
}