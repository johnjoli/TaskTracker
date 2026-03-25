package com.tasktracker.auth.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(min = 3, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "username can contain only letters, digits, dot, underscore and hyphen")
        String username,

        @NotBlank
        @Size(min = 8, max = 100)
        String password
) {
}
