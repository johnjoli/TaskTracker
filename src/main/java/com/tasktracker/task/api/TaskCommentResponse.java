package com.tasktracker.task.api;

import java.time.LocalDateTime;

public record TaskCommentResponse(
        Long id,
        String text,
        String author,
        LocalDateTime createdAt
) {
}
