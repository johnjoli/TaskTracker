package com.tasktracker.task.api;

import com.tasktracker.task.domain.TaskPriority;
import com.tasktracker.task.domain.TaskStatus;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDateTime dueDate,
        String createdBy,
        String assignee,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
