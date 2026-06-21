package com.gdb.users.repository.impl;

import com.gdb.users.domain.model.User;
import com.gdb.users.repository.UserRepository;
import com.gdb.users.repository.mapper.UserRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (username, login_id, password, role, is_active) VALUES (?, ?, ?, ?, ?) RETURNING *";

        return jdbcTemplate.queryForObject(sql, userRowMapper,
                user.getUsername(),
                user.getLoginId(),
                user.getPassword(),
                user.getRole(),
                user.getIsActive() != null ? user.getIsActive() : true);
    }

    @Override
    public void update(User user) {
        StringBuilder sql = new StringBuilder("UPDATE users SET updated_at = CURRENT_TIMESTAMP");
        List<Object> params = new ArrayList<>();

        if (user.getUsername() != null) {
            sql.append(", username = ?");
            params.add(user.getUsername());
        }
        if (user.getPassword() != null) {
            sql.append(", password = ?");
            params.add(user.getPassword());
        }
        if (user.getRole() != null) {
            sql.append(", role = ?");
            params.add(user.getRole());
        }

        sql.append(" WHERE login_id = ?");
        params.add(user.getLoginId());

        jdbcTemplate.update(sql.toString(), params.toArray());
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        return users.stream().findFirst();
    }

    @Override
    public Optional<User> findByLoginId(String loginId) {
        String sql = "SELECT * FROM users WHERE login_id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, loginId);
        return users.stream().findFirst();
    }

    @Override
    public List<User> findAll(String role, Boolean isActive) {
        StringBuilder sql = new StringBuilder("SELECT * FROM users");
        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        if (role != null) {
            conditions.add("role = ?");
            params.add(role);
        }
        if (isActive != null) {
            conditions.add("is_active = ?");
            params.add(isActive);
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        return jdbcTemplate.query(sql.toString(), userRowMapper, params.toArray());
    }

    @Override
    public boolean existsByLoginId(String loginId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM users WHERE login_id = ?", Integer.class, loginId);
        return count != null && count > 0;
    }

    @Override
    public void updateStatus(String loginId, boolean isActive) {
        String sql = "UPDATE users SET is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE login_id = ?";
        jdbcTemplate.update(sql, isActive, loginId);
    }
}
