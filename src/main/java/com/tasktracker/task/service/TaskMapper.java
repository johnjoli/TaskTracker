package com.tasktracker.task.service;

import com.tasktracker.auth.domain.AppUser;
import com.tasktracker.task.api.TaskRequest;
import com.tasktracker.task.api.TaskResponse;
import com.tasktracker.task.api.TaskPatchRequest;
import com.tasktracker.task.entity.Task;

final class TaskMapper {

    private TaskMapper() {
    }

    static TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                usernameOf(task.getCreatedBy()),
                usernameOf(task.getAssignee()),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    static void updateTask(Task task, TaskRequest request) {
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
    }

    static void patchTask(Task task, TaskPatchRequest request) {
        if (request.title() != null) {
            task.setTitle(request.title());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }
    }

    private static String usernameOf(AppUser user) {
        return user == null ? null : user.getUsername();
    }
}
