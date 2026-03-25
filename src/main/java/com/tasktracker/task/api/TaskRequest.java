package com.tasktracker.task.api;

import com.tasktracker.task.domain.TaskPriority;
import com.tasktracker.task.domain.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record TaskRequest(
        @NotBlank
        @Size(max = 120)
        String title,

        @Size(max = 1000)
        String description,

        @NotNull
        TaskStatus status,

        @NotNull
        TaskPriority priority,

        LocalDateTime dueDate,

        @Size(max = 50)
        String assigneeUsername
) {
}
