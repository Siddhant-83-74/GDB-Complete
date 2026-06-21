package com.gdb.users.repository.mapper;

import com.gdb.users.domain.model.User;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .userId(rs.getLong("user_id"))
                .username(rs.getString("username"))
                .loginId(rs.getString("login_id"))
                .password(rs.getString("password"))
                .role(rs.getString("role"))
                .isActive(rs.getBoolean("is_active"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }
}
