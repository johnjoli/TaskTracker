package com.tasktracker.auth.api;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String username,
        String role,
        LocalDateTime createdAt
) {
}
