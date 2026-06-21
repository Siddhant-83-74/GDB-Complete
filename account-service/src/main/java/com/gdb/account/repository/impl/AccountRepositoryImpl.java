package com.gdb.account.repository.impl;

import com.gdb.account.domain.model.Account;
import com.gdb.account.repository.AccountRepository;
import com.gdb.account.repository.mapper.AccountRowMapper;
import com.gdb.account.util.SqlLoader;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of AccountRepository using NamedParameterJdbcTemplate.
 */
@Repository
public class AccountRepositoryImpl implements AccountRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    // These would ideally be loaded from the db/query/account_queries.sql file at
    // runtime
    // For brevity in this initial implementation, I'm defining the logic
    // but the requirement is to EXTERNALIZE them.

    public AccountRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Account save(Account account) {
        String sql = SqlLoader.get("SAVE_ACCOUNT");

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("accountNumber", account.getAccountNumber())
                .addValue("accountType", account.getAccountType())
                .addValue("name", account.getName())
                .addValue("pinHash", account.getPinHash())
                .addValue("balance", account.getBalance())
                .addValue("privilege", account.getPrivilege())
                .addValue("bankName", account.getBankName())
                .addValue("bankBranch", account.getBankBranch())
                .addValue("ifscCode", account.getIfscCode())
                .addValue("isActive", account.getIsActive());

        jdbcTemplate.update(sql, params);
        return account;
    }

    @Override
    public Long generateAccountNumber() {
        String sql = SqlLoader.get("GET_NEXT_ACCOUNT_NUMBER");
        return jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Long.class);
    }

    @Override
    public void saveSavingsDetails(Account account) {
        String sql = SqlLoader.get("SAVE_SAVINGS_DETAILS");

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("accountNumber", account.getAccountNumber())
                .addValue("dob", account.getSavingsDetails().getDateOfBirth())
                .addValue("gender", account.getSavingsDetails().getGender())
                .addValue("phoneNo", account.getSavingsDetails().getPhoneNo())
                .addValue("aadharNumber", account.getSavingsDetails().getAadharNumber());

        jdbcTemplate.update(sql, params);
    }

    @Override
    public void saveCurrentDetails(Account account) {
        String sql = SqlLoader.get("SAVE_CURRENT_DETAILS");

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("accountNumber", account.getAccountNumber())
                .addValue("companyName", account.getCurrentDetails().getCompanyName())
                .addValue("website", account.getCurrentDetails().getWebsite())
                .addValue("registrationNo", account.getCurrentDetails().getRegistrationNo());

        jdbcTemplate.update(sql, params);
    }

    @Override
    public Optional<Account> findByAccountNumber(Long accountNumber) {
        String sql = SqlLoader.get("FIND_BY_ACCOUNT_NUMBER");
        MapSqlParameterSource params = new MapSqlParameterSource("accountNumber", accountNumber);

        List<Account> results = jdbcTemplate.query(sql, params, new AccountRowMapper());
        if (results.isEmpty())
            return Optional.empty();

        Account account = results.get(0);
        // Load specific details based on type
        if ("SAVINGS".equals(account.getAccountType())) {
            loadSavingsDetails(account);
        } else {
            loadCurrentDetails(account);
        }
        return Optional.of(account);
    }

    private void loadSavingsDetails(Account account) {
        String sql = SqlLoader.get("LOAD_SAVINGS_DETAILS");
        jdbcTemplate.query(sql, new MapSqlParameterSource("accountNumber", account.getAccountNumber()), rs -> {
            account.setSavingsDetails(Account.SavingsDetails.builder()
                    .dateOfBirth(rs.getString("date_of_birth"))
                    .gender(rs.getString("gender"))
                    .phoneNo(rs.getString("phone_no"))
                    .aadharNumber(rs.getString("aadhar_number"))
                    .build());
        });
    }

    private void loadCurrentDetails(Account account) {
        String sql = SqlLoader.get("LOAD_CURRENT_DETAILS");
        jdbcTemplate.query(sql, new MapSqlParameterSource("accountNumber", account.getAccountNumber()), rs -> {
            account.setCurrentDetails(Account.CurrentDetails.builder()
                    .companyName(rs.getString("company_name"))
                    .website(rs.getString("website"))
                    .registrationNo(rs.getString("registration_no"))
                    .build());
        });
    }

    @Override
    public List<Account> findAll(String type, String privilege, Boolean isActive) {
        StringBuilder sql = new StringBuilder(SqlLoader.get("FIND_ALL_ACCOUNTS"));
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (type != null) {
            sql.append(" AND account_type = :type");
            params.addValue("type", type);
        }
        if (privilege != null) {
            sql.append(" AND privilege = :privilege");
            params.addValue("privilege", privilege);
        }
        if (isActive != null) {
            sql.append(" AND is_active = :isActive");
            params.addValue("isActive", isActive);
        }

        return jdbcTemplate.query(sql.toString(), params, new AccountRowMapper());
    }

    @Override
    public void updateBalance(Long accountNumber, BigDecimal newBalance) {
        String sql = SqlLoader.get("UPDATE_BALANCE");
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("balance", newBalance)
                .addValue("accountNumber", accountNumber));
    }

    @Override
    public void updateStatus(Long accountNumber, boolean isActive) {
        String sql = SqlLoader.get("UPDATE_STATUS");
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("isActive", isActive)
                .addValue("accountNumber", accountNumber));
    }

    @Override
    public boolean existsByNameAndDob(String name, String dob) {
        String sql = SqlLoader.get("CHECK_DUPLICATE_SAVINGS");
        Integer count = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("dob", dob), Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByRegistrationNo(String registrationNo) {
        String sql = SqlLoader.get("CHECK_DUPLICATE_CURRENT");
        Integer count = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("registrationNo", registrationNo),
                Integer.class);
        return count != null && count > 0;
    }
}
