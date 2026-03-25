package com.tasktracker.auth.api;

public record AuthResponse(
        String token,
        String username,
        String role
) {
}
