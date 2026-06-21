package com.gdb.transactions.repository.impl;

import com.gdb.transactions.domain.model.FundTransfer;
import com.gdb.transactions.repository.FundTransferRepository;
import com.gdb.transactions.repository.mapper.FundTransferRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of FundTransferRepository.
 * 
 * TODO: MOD3-CR-01: Upgrade to Named Parameters.
 * Trainee task: Swap JdbcTemplate with NamedParameterJdbcTemplate, and rewrite 
 * queries to use named parameter queries (e.g. :fromAccount, :toAccount, etc.).
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class FundTransferRepositoryImpl implements FundTransferRepository {

    private final NamedParameterJdbcTemplate namedJdbc;
    private final FundTransferRowMapper rowMapper;

    @Override
    public FundTransfer save(FundTransfer fundTransfer) {
        if (fundTransfer.getId() == null) {
            return insert(fundTransfer);
        } else {
            return update(fundTransfer);
        }
    }

    private FundTransfer insert(FundTransfer fundTransfer) {
        String sql = """
            INSERT INTO fund_transfers (from_account, to_account, transfer_amount, transfer_mode)
            VALUES (:from_account, :to_account, :transfer_amount, :transfer_mode)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

//        jdbcTemplate.update(connection -> {
//            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
//            ps.setLong(1, fundTransfer.getFromAccount());
//            ps.setLong(2, fundTransfer.getToAccount());
//            ps.setBigDecimal(3, fundTransfer.getTransferAmount());
//            ps.setString(4, fundTransfer.getTransferMode().name());
//            return ps;
//        }, keyHolder);
//
//        Long id = keyHolder.getKey().longValue();
//        return findById(id).orElseThrow(() -> new RuntimeException("Failed to retrieve saved fund transfer"));

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("from_account", fundTransfer.getFromAccount())
                .addValue("to_account", fundTransfer.getToAccount())
                .addValue("transfer_amount", fundTransfer.getTransferAmount())
                .addValue("transfer_mode", fundTransfer.getTransferMode().name());

        namedJdbc.update(sql, params, keyHolder, new String[]{"id"});

        Long id = keyHolder.getKey().longValue();
        return findById(id).orElseThrow(() -> new RuntimeException("Failed to retrieve saved fund transfer"));
    }

    private FundTransfer update(FundTransfer fundTransfer) {
        String sql = """
            UPDATE fund_transfers 
            SET from_account = :from_account, to_account = :to_account, transfer_amount = :transfer_amount, transfer_mode = :transfer_mode
            WHERE id = :id
            """;

//        jdbcTemplate.update(sql,
//                fundTransfer.getFromAccount(),
//                fundTransfer.getToAccount(),
//                fundTransfer.getTransferAmount(),
//                fundTransfer.getTransferMode().name(),
//                fundTransfer.getId());
//
//        return findById(fundTransfer.getId()).orElseThrow(() -> new RuntimeException("Failed to retrieve updated fund transfer"));
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("from_account", fundTransfer.getFromAccount())
                .addValue("to_account", fundTransfer.getToAccount())
                .addValue("transfer_amount", fundTransfer.getTransferAmount())
                .addValue("transfer_mode", fundTransfer.getTransferMode().name())
                .addValue("id", fundTransfer.getId());

        namedJdbc.update(sql, params);
        return findById(fundTransfer.getId()).orElseThrow(() -> new RuntimeException("Failed to retrieve updated fund transfer"));
    }

    @Override
    public Optional<FundTransfer> findById(Long id) {
        String sql = """
            SELECT id, from_account, to_account, transfer_amount, transfer_mode, created_at, updated_at
            FROM fund_transfers
            WHERE id = :id
            """;

//        List<FundTransfer> results = jdbcTemplate.query(sql, rowMapper, id);
        List<FundTransfer> results = namedJdbc.query(sql, new MapSqlParameterSource("id", id), rowMapper);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public BigDecimal getDailyTransferAmount(Long accountNumber, LocalDate date) {
        String sql = """
            SELECT COALESCE(SUM(transfer_amount), 0)
            FROM fund_transfers
            WHERE from_account = :from_account AND DATE(created_at) = :date
            """;

//        BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class, accountNumber, date);
        // SQL: WHERE from_account = :accountNumber AND DATE(created_at) = :date
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("from_account", accountNumber)
                .addValue("date", date);
        BigDecimal result = namedJdbc.queryForObject(sql, params, BigDecimal.class);   // Integer.class for the count one
        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    public Integer getDailyTransferCount(Long accountNumber, LocalDate date) {
        String sql = """
            SELECT COUNT(*)
            FROM fund_transfers
            WHERE from_account = :from_account AND DATE(created_at) = :date
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("from_account", accountNumber)
                .addValue("date", date);
        Integer result = namedJdbc.queryForObject(sql, params, Integer.class);
        return result != null ? result : 0;
    }

    @Override
    public List<FundTransfer> findByAccount(Long accountNumber, int limit, int offset) {
        String sql = """
            SELECT id, from_account, to_account, transfer_amount, transfer_mode, created_at, updated_at
            FROM fund_transfers
            WHERE from_account = :account_no OR to_account =:account_no
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
            """;

        // TODO: MOD3-BUG-01: Parameter index out of range SQL Exception.
        // The SQL query checks: from_account = ? OR to_account = ?
        // Identify why this call throws an exception and fix the parameter binding.
        // Injected Bug: Only binding accountNumber once instead of twice.
//        return jdbcTemplate.query(sql, rowMapper, accountNumber, limit, offset);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("account_no", accountNumber)   // named once, used TWICE in SQL — bug gone
                .addValue("limit", limit)
                .addValue("offset", offset);

        return namedJdbc.query(sql, params, rowMapper);

    }

    @Override
    public List<FundTransfer> findAll(int limit, int offset) {
        String sql = """
            SELECT id, from_account, to_account, transfer_amount, transfer_mode, created_at, updated_at
            FROM fund_transfers
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
            """;

//        return jdbcTemplate.query(sql, rowMapper, limit, offset);

        // SQL: ... LIMIT :limit OFFSET :offset
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset);
        return namedJdbc.query(sql, params, rowMapper);

    }

    @Override
    public Long countAll() {
        String sql = "SELECT COUNT(*) FROM fund_transfers";
//        return jdbcTemplate.queryForObject(sql, Long.class);
        return namedJdbc.queryForObject(sql, new MapSqlParameterSource(), Long.class);
    }
}