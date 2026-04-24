package com.tasktracker.auth.service;

import com.tasktracker.auth.api.UserResponse;
import com.tasktracker.auth.domain.AppUser;

final class UserMapper {

    private UserMapper() {
    }

    static UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
