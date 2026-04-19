package com.tasktracker.task.service;

import com.tasktracker.auth.domain.AppUser;
import com.tasktracker.auth.repository.UserRepository;
import com.tasktracker.auth.service.CurrentUserService;
import com.tasktracker.common.api.PageResponse;
import com.tasktracker.common.exception.ResourceNotFoundException;
import com.tasktracker.task.api.TaskPatchRequest;
import com.tasktracker.task.api.TaskRequest;
import com.tasktracker.task.api.TaskResponse;
import com.tasktracker.task.domain.Task;
import com.tasktracker.task.domain.TaskPriority;
import com.tasktracker.task.domain.TaskStatus;
import com.tasktracker.task.repository.TaskRepository;
import com.tasktracker.task.repository.TaskSpecifications;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        AppUser currentUser = currentUserService.getCurrentUser();
        Task task = new Task();
        TaskMapper.updateTask(task, request);
        task.setCreatedBy(currentUser);
        task.setAssignee(resolveAssignee(request.assigneeUsername(), currentUser));
        return TaskMapper.toResponse(taskRepository.save(task));

    }

    public PageResponse<TaskResponse> findAll(
            TaskStatus status,
            TaskPriority priority,
            LocalDateTime dueDateFrom,
            LocalDateTime dueDateTo,
            String query,
            String createdBy,
            String assignee,
            Pageable pageable
    ) {
        Page<TaskResponse> page = taskRepository.findAll(
                        TaskSpecifications.hasStatus(status)
                                .and(TaskSpecifications.hasDueDateFrom(dueDateFrom))
                                .and(TaskSpecifications.hasDueDateTo(dueDateTo))
                                .and(TaskSpecifications.hasPriority(priority))
                                .and(TaskSpecifications.titleOrDescriptionContains(query))
                                .and(TaskSpecifications.hasCreatedBy(createdBy))
                                .and(TaskSpecifications.hasAssignee(assignee)),
                        pageable
                )
                .map(TaskMapper::toResponse);

        return PageResponse.from(page);
    }

    public TaskResponse findById(Long id) {
        return TaskMapper.toResponse(getTask(id));
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = getTask(id);
        verifyCanModify(task);
        TaskMapper.updateTask(task, request);
        task.setAssignee(resolveAssignee(request.assigneeUsername(), currentUserService.getCurrentUser()));
        return TaskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse patch(Long id, TaskPatchRequest request) {
        Task task = getTask(id);
        verifyCanModify(task);
        TaskMapper.patchTask(task, request);
        if (request.assigneeUsername() != null) {
            task.setAssignee(resolveAssignee(request.assigneeUsername(), currentUserService.getCurrentUser()));
        }
        return TaskMapper.toResponse(task);
    }

    @Transactional
    public void delete(Long id) {
        Task task = getTask(id);
        verifyCanModify(task);
        taskRepository.delete(task);
    }

    private Task getTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id=%d was not found".formatted(id)));
    }

    private AppUser resolveAssignee(String assigneeUsername, AppUser currentUser) {
        if (assigneeUsername == null || assigneeUsername.isBlank()) {
            return null;
        }

        AppUser assignee = userRepository.findByUsername(assigneeUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User with username=%s was not found".formatted(assigneeUsername)));

        if (!currentUserService.isAdmin() && !assignee.getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only admins can assign tasks to other users");
        }

        return assignee;
    }

    private void verifyCanModify(Task task) {
        AppUser currentUser = currentUserService.getCurrentUser();
        boolean isCreator = task.getCreatedBy() != null && task.getCreatedBy().getId().equals(currentUser.getId());
        if (!isCreator && !currentUserService.isAdmin()) {
            throw new AccessDeniedException("Only the creator or admin can modify this task");
        }
    }
}
