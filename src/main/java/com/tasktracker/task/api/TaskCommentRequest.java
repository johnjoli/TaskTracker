package com.tasktracker.task.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskCommentRequest(
        @NotBlank
        @Size(max = 1000)
        String text
) {
}
