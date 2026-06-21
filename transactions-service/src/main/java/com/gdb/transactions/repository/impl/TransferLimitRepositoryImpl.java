package com.gdb.transactions.repository.impl;

import com.gdb.transactions.domain.enums.PrivilegeLevel;
import com.gdb.transactions.domain.model.TransferLimit;
import com.gdb.transactions.repository.TransferLimitRepository;
import com.gdb.transactions.repository.mapper.TransferLimitRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of TransferLimitRepository.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TransferLimitRepositoryImpl implements TransferLimitRepository {

    private final JdbcTemplate jdbcTemplate;
    private final TransferLimitRowMapper rowMapper;

    @Override
    public Optional<TransferLimit> findByPrivilege(PrivilegeLevel privilege) {
        String sql = """
            SELECT privilege, daily_limit, per_transaction_limit, transaction_limit, created_at, updated_at
            FROM transfer_limits
            WHERE privilege = ?
            """;

        List<TransferLimit> results = jdbcTemplate.query(sql, rowMapper, privilege.name());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<TransferLimit> findAll() {
        String sql = """
            SELECT privilege, daily_limit, per_transaction_limit, transaction_limit, created_at, updated_at
            FROM transfer_limits
            ORDER BY privilege
            """;

        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public TransferLimit save(TransferLimit transferLimit) {
        String sql = """
            INSERT INTO transfer_limits (privilege, daily_limit, per_transaction_limit, transaction_limit)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (privilege) DO UPDATE SET
                daily_limit = EXCLUDED.daily_limit,
                per_transaction_limit = EXCLUDED.per_transaction_limit,
                transaction_limit = EXCLUDED.transaction_limit
            """;

        jdbcTemplate.update(sql,
                transferLimit.getPrivilege().name(),
                transferLimit.getDailyLimit(),
                transferLimit.getPerTransactionLimit(),
                transferLimit.getTransactionLimit());

        return findByPrivilege(transferLimit.getPrivilege())
                .orElseThrow(() -> new RuntimeException("Failed to retrieve saved transfer limit"));
    }

    @Override
    public void deleteByPrivilege(PrivilegeLevel privilege) {
        String sql = "DELETE FROM transfer_limits WHERE privilege = ?";
        jdbcTemplate.update(sql, privilege.name());
    }
}