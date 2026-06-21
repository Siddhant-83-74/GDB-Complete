package com.gdb.users.service.impl;

import com.gdb.users.constants.UserConstants;
import com.gdb.users.domain.model.User;
import com.gdb.users.dto.request.AddUserRequest;
import com.gdb.users.dto.request.EditUserRequest;
import com.gdb.users.dto.response.VerifyCredentialsResponse;
import com.gdb.users.exception.UserNotFoundException;
import com.gdb.users.exception.UserAlreadyExistsException;
import com.gdb.users.repository.UserRepository;
import com.gdb.users.repository.SecurityAlertRepository;
import com.gdb.users.domain.model.SecurityAlertLog;
import com.gdb.users.service.AuditService;
import com.gdb.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt; // Use jbcrypt directly
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuditService auditService;
    private final SecurityAlertRepository alertRepository;

    @Override
    @Transactional
    public User addUser(AddUserRequest request) {
        log.info("Adding new user with loginId: {}", request.getLoginId());

        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new UserAlreadyExistsException("User with loginId " + request.getLoginId() + " already exists");
        }

        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt(UserConstants.BCRYPT_SALT_ROUNDS));
        String role = request.getRole() != null ? request.getRole() : UserConstants.ROLE_TELLER;

        User user = User.builder()
                .username(request.getUsername())
                .loginId(request.getLoginId())
                .password(hashedPassword)
                .role(role)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        auditService.logAction(savedUser.getUserId(), UserConstants.ACTION_CREATE, null, savedUser);
        return savedUser;
    }

    @Override
    @Transactional
    public User updateUser(String loginId, EditUserRequest request) {
        log.info("Updating user with loginId: {}", loginId);

        User existingUser = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UserNotFoundException("User not found with loginId: " + loginId));

        User.UserBuilder userBuilder = User.builder()
                .loginId(loginId); // Identifying key

        boolean updated = false;

        if (request.getUsername() != null) {
            userBuilder.username(request.getUsername());
            updated = true;
        }

        if (request.getPassword() != null) {
            String hashedPassword = BCrypt.hashpw(request.getPassword(),
                    BCrypt.gensalt(UserConstants.BCRYPT_SALT_ROUNDS));
            userBuilder.password(hashedPassword);
            updated = true;
        }

        if (request.getRole() != null) {
            userBuilder.role(request.getRole());
            updated = true;
        }

        if (updated) {
            User userToUpdate = userBuilder.build();
            userRepository.update(userToUpdate);
            // Fetch updated user to return
            User updatedUser = userRepository.findByLoginId(loginId).get();
            auditService.logAction(updatedUser.getUserId(), UserConstants.ACTION_UPDATE, existingUser, updatedUser);
            return updatedUser;
        }

        return existingUser;
    }

    @Override
    public User getUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UserNotFoundException("User not found with loginId: " + loginId));
    }

    @Override
    public List<User> listUsers(String role, Boolean isActive) {
        return userRepository.findAll(role, isActive);
    }

    @Override
    @Transactional
    public void activateUser(String loginId) {
        User user = getUserByLoginId(loginId);
        if (Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalStateException("User is already active");
        }
        userRepository.updateStatus(loginId, true);
        auditService.logAction(user.getUserId(), UserConstants.ACTION_ACTIVATE, "inactive", "active");
    }

    @Override
    @Transactional
    public void inactivateUser(String loginId) {
        User user = getUserByLoginId(loginId);
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new IllegalStateException("User is already inactive");
        }
        userRepository.updateStatus(loginId, false);
        auditService.logAction(user.getUserId(), UserConstants.ACTION_INACTIVATE, "active", "inactive");
    }

    @Override
    public VerifyCredentialsResponse verifyCredentials(String loginId, String password) {
        Optional<User> userOpt = userRepository.findByLoginId(loginId);

        if (userOpt.isEmpty()) {
            return VerifyCredentialsResponse.builder().isValid(false).isActive(false).build();
        }

        User user = userOpt.get();
        if (Boolean.FALSE.equals(user.getIsActive())) {
            return VerifyCredentialsResponse.builder()
                    .isValid(false)
                    .userId(user.getUserId())
                    .role(user.getRole())
                    .isActive(false) // User exists but inactive
                    .build();
        }

        boolean passwordMatch;
        try {
            passwordMatch = BCrypt.checkpw(password, user.getPassword());
        } catch (IllegalArgumentException e) {
            log.error("Invalid BCrypt hash for user: {}. The stored password hash is corrupted.", loginId, e);
            return VerifyCredentialsResponse.builder()
                    .isValid(false)
                    .userId(user.getUserId())
                    .role(user.getRole())
                    .isActive(true)
                    .build();
        }

        if (!passwordMatch) {
            alertRepository.save(SecurityAlertLog.builder()
                    .userId(user.getUserId())
                    .message("Failed login attempt for loginId: " + loginId)
                    .alertDate(LocalDateTime.now())
                    .build());
        }

        return VerifyCredentialsResponse.builder()
                .isValid(passwordMatch)
                .userId(user.getUserId())
                .role(user.getRole())
                .isActive(true)
                .build();
    }
}
