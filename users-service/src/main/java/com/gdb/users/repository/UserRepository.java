package com.gdb.users.repository;

import com.gdb.users.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);

    void update(User user);

    Optional<User> findById(Long id);

    Optional<User> findByLoginId(String loginId);

    List<User> findAll(String role, Boolean isActive);

    boolean existsByLoginId(String loginId);

    void updateStatus(String loginId, boolean isActive);
}
