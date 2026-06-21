package com.gdb.users.mapper;

import com.gdb.users.domain.model.User;
import com.gdb.users.dto.response.UserResponse;
import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        if (user == null)
            return null;
        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .loginId(user.getLoginId())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static List<UserResponse> toResponseList(List<User> users) {
        return users.stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }
}
