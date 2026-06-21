package com.gdb.account.repository.mapper;

import com.gdb.account.domain.model.Account;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RowMapper for Account entity.
 */
public class AccountRowMapper implements RowMapper<Account> {

    @Override
    public Account mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Account.builder()
                .id(rs.getLong("id"))
                .accountNumber(rs.getLong("account_number"))
                .accountType(rs.getString("account_type"))
                .name(rs.getString("name"))
                .pinHash(rs.getString("pin_hash"))
                .balance(rs.getBigDecimal("balance"))
                .privilege(rs.getString("privilege"))
                .bankName(rs.getString("bank_name"))
                .bankBranch(rs.getString("bank_branch"))
                .ifscCode(rs.getString("ifsc_code"))
                .isActive(rs.getBoolean("is_active"))
                .activatedDate(
                        rs.getTimestamp("activated_date") != null ? rs.getTimestamp("activated_date").toLocalDateTime()
                                : null)
                .closedDate(rs.getTimestamp("closed_date") != null ? rs.getTimestamp("closed_date").toLocalDateTime()
                        : null)
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }
}
