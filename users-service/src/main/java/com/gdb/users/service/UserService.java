package com.gdb.users.service;

import com.gdb.users.domain.model.User;
import com.gdb.users.dto.request.AddUserRequest;
import com.gdb.users.dto.request.EditUserRequest;
import com.gdb.users.dto.response.VerifyCredentialsResponse;
import java.util.List;

public interface UserService {

    User addUser(AddUserRequest request);

    User updateUser(String loginId, EditUserRequest request);

    User getUserByLoginId(String loginId);

    List<User> listUsers(String role, Boolean isActive);

    void activateUser(String loginId);

    void inactivateUser(String loginId);

    VerifyCredentialsResponse verifyCredentials(String loginId, String password);
}
