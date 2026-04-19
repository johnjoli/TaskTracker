package com.tasktracker.task.service;

import com.tasktracker.task.api.TaskPatchRequest;
import com.tasktracker.task.api.TaskRequest;
import com.tasktracker.task.domain.Task;
import com.tasktracker.task.domain.TaskPriority;
import com.tasktracker.task.domain.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TaskMapperTest {

    @Test
    void shouldUpdateTaskFromRequest() {
        Task task = new Task();
        TaskRequest request = new TaskRequest(
                "Finish backend",
                "Add due date filter",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                LocalDateTime.of(2025, 3, 30, 18, 0),
                "alice"
        );

        TaskMapper.updateTask(task, request);

        assertEquals("Finish backend", task.getTitle());
        assertEquals("Add due date filter", task.getDescription());
        assertEquals(TaskPriority.HIGH, task.getPriority());
        assertEquals(LocalDateTime.of(2025, 3, 30, 18, 0), task.getDueDate());
    }

    @Test
    void shouldPatchOnlyNonNullFields() {
        Task task = new Task();
        task.setTitle("Old title");
        task.setDescription("Old description");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task.setDueDate(LocalDateTime.of(2025, 3, 10, 12, 0));

        TaskPatchRequest request = new TaskPatchRequest(
                null,
                null,
                TaskStatus.IN_PROGRESS,
                null,
                LocalDateTime.of(2025, 3, 25, 18, 0),
                null
        );

        TaskMapper.patchTask(task, request);

        assertEquals("Old title", task.getTitle());
        assertEquals("Old description", task.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(TaskPriority.MEDIUM, task.getPriority());
        assertEquals(LocalDateTime.of(2025, 3, 25, 18, 0), task.getDueDate());
    }
}
