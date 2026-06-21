package com.gdb.transactions.repository.impl;

import com.gdb.transactions.domain.enums.TransactionType;
import com.gdb.transactions.domain.model.TransactionLog;
import com.gdb.transactions.repository.TransactionLogRepository;
import com.gdb.transactions.repository.mapper.TransactionLogRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of TransactionLogRepository.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TransactionLogRepositoryImpl implements TransactionLogRepository {

    private final JdbcTemplate jdbcTemplate;
    private final TransactionLogRowMapper rowMapper;

    @Override
    public TransactionLog save(TransactionLog transactionLog) {
        if (transactionLog.getId() == null) {
            return insert(transactionLog);
        } else {
            return update(transactionLog);
        }
    }

    private TransactionLog insert(TransactionLog transactionLog) {
        String sql = """
                INSERT INTO transaction_logging (account_number, amount, transaction_type, reference_id, description, mode, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
            ps.setLong(1, transactionLog.getAccountNumber());
            ps.setBigDecimal(2, transactionLog.getAmount());
            ps.setString(3, transactionLog.getTransactionType().name());
            ps.setObject(4, transactionLog.getReferenceId());
            ps.setString(5, transactionLog.getDescription());
            ps.setString(6, transactionLog.getMode());
            ps.setString(7, transactionLog.getStatus() != null ? transactionLog.getStatus() : "SUCCESS");
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKeyAs(Number.class);
        Long id = (key != null) ? key.longValue() : null;

        if (id == null && keyHolder.getKeys() != null) {
            id = (Long) keyHolder.getKeys().get("id");
        }

        if (id == null) {
            throw new RuntimeException("Failed to retrieve generated ID for transaction log");
        }

        return findById(id).orElseThrow(() -> new RuntimeException("Failed to retrieve saved transaction log"));
    }

    private TransactionLog update(TransactionLog transactionLog) {
        String sql = """
                UPDATE transaction_logging
                SET account_number = ?, amount = ?, transaction_type = ?, reference_id = ?, description = ?, mode = ?, status = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql,
                transactionLog.getAccountNumber(),
                transactionLog.getAmount(),
                transactionLog.getTransactionType().name(),
                transactionLog.getReferenceId(),
                transactionLog.getDescription(),
                transactionLog.getMode(),
                transactionLog.getStatus(),
                transactionLog.getId());

        return findById(transactionLog.getId())
                .orElseThrow(() -> new RuntimeException("Failed to retrieve updated transaction log"));
    }

    @Override
    public Optional<TransactionLog> findById(Long id) {
        String sql = """
                SELECT id, account_number, amount, transaction_type, reference_id, description, mode, status, created_at, updated_at
                FROM transaction_logging
                WHERE id = ?
                """;

        List<TransactionLog> results = jdbcTemplate.query(sql, rowMapper, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<TransactionLog> findByAccountNumber(Long accountNumber, int limit, int offset) {
        String sql = """
                SELECT id, account_number, amount, transaction_type, reference_id, description, mode, status, created_at, updated_at
                FROM transaction_logging
                WHERE account_number = ?
                ORDER BY created_at DESC
                LIMIT ? OFFSET ?
                """;

        return jdbcTemplate.query(sql, rowMapper, accountNumber, limit, offset);
    }

    @Override
    public List<TransactionLog> findByAccountNumberAndType(Long accountNumber, TransactionType type, int limit,
            int offset) {
        String sql = """
                SELECT id, account_number, amount, transaction_type, reference_id, description, mode, status, created_at, updated_at
                FROM transaction_logging
                WHERE account_number = ? AND transaction_type = ?
                ORDER BY created_at DESC
                LIMIT ? OFFSET ?
                """;

        return jdbcTemplate.query(sql, rowMapper, accountNumber, type.name(), limit, offset);
    }

    @Override
    public List<TransactionLog> findByAccountNumberAndDateRange(Long accountNumber, LocalDate startDate,
            LocalDate endDate, int limit, int offset) {
        String sql = """
                SELECT id, account_number, amount, transaction_type, reference_id, description, mode, status, created_at, updated_at
                FROM transaction_logging
                WHERE account_number = ? AND DATE(created_at) BETWEEN ? AND ?
                ORDER BY created_at DESC
                LIMIT ? OFFSET ?
                """;

        return jdbcTemplate.query(sql, rowMapper, accountNumber, startDate, endDate, limit, offset);
    }

    @Override
    public List<TransactionLog> findAll(int limit, int offset) {
        String sql = """
                SELECT id, account_number, amount, transaction_type, reference_id, description, mode, status, created_at, updated_at
                FROM transaction_logging
                ORDER BY created_at DESC
                LIMIT ? OFFSET ?
                """;

        return jdbcTemplate.query(sql, rowMapper, limit, offset);
    }

    @Override
    public Long countAll() {
        String sql = "SELECT COUNT(*) FROM transaction_logging";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    @Override
    public Long countByAccountNumber(Long accountNumber) {
        String sql = "SELECT COUNT(*) FROM transaction_logging WHERE account_number = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, accountNumber);
    }
}