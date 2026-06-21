package com.gdb.users.controller;

import com.gdb.users.domain.model.User;
import com.gdb.users.dto.request.*;
import com.gdb.users.dto.response.ApiResponse;
import com.gdb.users.dto.response.UserResponse;
import com.gdb.users.dto.response.VerifyCredentialsResponse;
import com.gdb.users.service.UserService;
import com.gdb.users.mapper.UserMapper;
import com.gdb.users.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;

    // Public API Endpoints

    @PostMapping("/api/v1/users")
    public ResponseEntity<UserResponse> addUser(@Valid @RequestBody AddUserRequest request) {
        SecurityUtils.checkAdminRole();
        log.info("Received request to create user: {}", request.getLoginId());
        User user = userService.addUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toResponse(user));
    }

    @GetMapping("/api/v1/users/{loginId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String loginId) {
        SecurityUtils.checkAdminOrTellerRole();
        log.info("Received request to get user: {}", loginId);
        User user = userService.getUserByLoginId(loginId);
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }

    @GetMapping("/api/v1/users")
    public ResponseEntity<List<UserResponse>> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive) {
        SecurityUtils.checkAdminOrTellerRole();
        log.info("Received request to list users. Role: {}, IsActive: {}", role, isActive);
        List<User> users = userService.listUsers(role, isActive);
        return ResponseEntity.ok(UserMapper.toResponseList(users));
    }

    @PutMapping("/api/v1/users/{loginId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String loginId,
            @Valid @RequestBody EditUserRequest request) {
        SecurityUtils.checkAdminRole();
        log.info("Received request to update user: {}", loginId);
        User user = userService.updateUser(loginId, request);
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }

    @PostMapping("/api/v1/users/activate")
    public ResponseEntity<ApiResponse> activateUser(@Valid @RequestBody ActivateUserRequest request) {
        SecurityUtils.checkAdminRole();
        log.info("Received request to activate user: {}", request.getLoginId());
        userService.activateUser(request.getLoginId());
        return ResponseEntity.ok(ApiResponse.builder()
                .status("success")
                .message("User successfully activated: " + request.getLoginId())
                .build());
    }

    @PostMapping("/api/v1/users/inactivate")
    public ResponseEntity<ApiResponse> inactivateUser(@Valid @RequestBody InactivateUserRequest request) {
        SecurityUtils.checkAdminRole();
        log.info("Received request to inactivate user: {}", request.getLoginId());
        userService.inactivateUser(request.getLoginId());
        return ResponseEntity.ok(ApiResponse.builder()
                .status("success")
                .message("User successfully inactivated: " + request.getLoginId())
                .build());
    }

    // Internal API Endpoints

    @PostMapping("/internal/v1/users/verify")
    public ResponseEntity<VerifyCredentialsResponse> verifyCredentials(
            @Valid @RequestBody VerifyCredentialsRequest request) {
        log.info("Received internal request to verify credentials for: {}", request.getLoginId());
        VerifyCredentialsResponse response = userService.verifyCredentials(request.getLoginId(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/internal/v1/users/{loginId}/status")
    public ResponseEntity<UserResponse> getUserStatus(@PathVariable String loginId) {
        log.info("Received internal request for user status: {}", loginId);
        User user = userService.getUserByLoginId(loginId);
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }
}
