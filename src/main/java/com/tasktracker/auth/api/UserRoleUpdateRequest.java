package com.tasktracker.auth.api;

import com.tasktracker.auth.domain.Role;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateRequest(
        @NotNull
        Role role
) {
}
