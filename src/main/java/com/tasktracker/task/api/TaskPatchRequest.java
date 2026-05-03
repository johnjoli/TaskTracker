package com.tasktracker.task.api;

import com.tasktracker.task.entity.TaskPriority;
import com.tasktracker.task.entity.TaskStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record TaskPatchRequest(
        @Size(min = 1, max = 120)
        String title,

        @Size(max = 1000)
        String description,

        TaskStatus status,
        TaskPriority priority,
        LocalDateTime dueDate,

        @Size(max = 50)
        String assigneeUsername
) {
}
