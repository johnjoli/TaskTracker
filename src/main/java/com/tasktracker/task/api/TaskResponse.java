package com.tasktracker.task.api;

import com.tasktracker.task.entity.TaskPriority;
import com.tasktracker.task.entity.TaskStatus;

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
