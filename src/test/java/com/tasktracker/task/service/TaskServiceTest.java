package com.tasktracker.task.service;

import com.tasktracker.auth.repository.UserRepository;
import com.tasktracker.auth.service.CurrentUserService;
import com.tasktracker.common.api.PageResponse;
import com.tasktracker.task.api.TaskCommentRequest;
import com.tasktracker.task.api.TaskCommentResponse;
import com.tasktracker.task.entity.Task;
import com.tasktracker.task.entity.TaskComment;
import com.tasktracker.task.entity.TaskPriority;
import com.tasktracker.task.entity.TaskStatus;
import com.tasktracker.task.repository.TaskCommentRepository;
import com.tasktracker.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.tasktracker.common.exception.ResourceNotFoundException;

import com.tasktracker.auth.domain.AppUser;
import com.tasktracker.task.api.TaskRequest;
import com.tasktracker.task.api.TaskResponse;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceTest.class);
    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskCommentRepository taskCommentRepository;


    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldReturnTaskById() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Finish backend");
        task.setDescription("Add service test");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setDueDate(LocalDateTime.of(2025, 3, 30, 18, 0));

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        var response = taskService.findById(1L);

        assertEquals(1L, response.id());
        assertEquals("Finish backend", response.title());
        assertEquals("Add service test", response.description());
        assertEquals(TaskStatus.TODO, response.status());
        assertEquals(TaskPriority.HIGH, response.priority());
        assertEquals(LocalDateTime.of(2025, 3, 30, 18, 0), response.dueDate());
    }

    @Test
    void shouldThrowWhenTaskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.findById(1L));
    }

    @Test
    void shouldCreateTaskAssignedToCurrentUser() {
        AppUser currentUser = new AppUser();
        currentUser.setId(1L);
        currentUser.setUsername("alice");

        TaskRequest request = new TaskRequest(
          "Finish backend",
          "Add create service test",
          TaskStatus.TODO,
          TaskPriority.HIGH,
          LocalDateTime.of(2025, 3, 30, 18, 0),
          "alice"
        );

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(currentUserService.isAdmin()).thenReturn(false);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(currentUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.create(request);

        assertEquals("Finish backend", response.title());
        assertEquals("Add create service test", response.description());
        assertEquals(TaskStatus.TODO, response.status());
        assertEquals(TaskPriority.HIGH, response.priority());
        assertEquals(LocalDateTime.of(2025, 3, 30, 18, 0), response.dueDate());
        assertEquals("alice", response.createdBy());
        assertEquals("alice", response.assignee());
    }

    @Test
    void shouldThrowWhenUserAssignsTaskToAnotherUserWithoutAdminRole() {
        AppUser currentUser = new AppUser();
        currentUser.setId(1L);
        currentUser.setUsername("alice");

        AppUser anotherUser = new AppUser();
        anotherUser.setId(2L);
        anotherUser.setUsername("bob");

        TaskRequest request = new TaskRequest(
                "Finish backend",
                "Try illegal assignment",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                LocalDateTime.of(2025, 3, 30, 18, 0),
                "bob"
        );

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(currentUserService.isAdmin()).thenReturn(false);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(anotherUser));

        assertThrows(AccessDeniedException.class, () -> taskService.create(request));
    }

    @Test
    void shouldCompleteTaskWhenCurrentUSerIsCreator() {
        AppUser currentUser = new AppUser();
        currentUser.setId(1L);
        currentUser.setUsername("alice");

        Task task = new Task();
        task.setId(10L);
        task.setTitle("Finish backend");
        task.setDescription("Implement complete endpoint");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setPriority(TaskPriority.HIGH);
        task.setCreatedBy(currentUser);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.complete(10L);

        assertEquals(TaskStatus.DONE, response.status());
        assertEquals("Finish backend", response.title());
    }

    @Test
    void shouldUseCurrentUserWhenOnlyMineIsTrue() {
        AppUser currentUser = new AppUser();
        currentUser.setId(1L);
        currentUser.setUsername("alice");

        Page<Task> taskPage = new PageImpl<>(List.of());

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(taskRepository.findAll(
                ArgumentMatchers.<Specification<Task>>any(),
                any(Pageable.class)
        )).thenReturn(taskPage);



        PageResponse<TaskResponse> response = taskService.findAll(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                Pageable.unpaged()
        );

        assertEquals(0, response.content().size());
        verify(currentUserService).getCurrentUser();
        verify(taskRepository).findAll(
                ArgumentMatchers.<Specification<Task>>any(),
                any(Pageable.class)
        );

    }

    @Test
    void shouldAssignTaskToCurrentUser() {
        AppUser currentUser = new AppUser();
        currentUser.setId(1L);
        currentUser.setUsername("alice");

        Task task = new Task();
        task.setId(10L);
        task.setTitle("Finish backend");
        task.setDescription("Implement assign-to-me");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setCreatedBy(currentUser);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.assignToMe(10L);

        assertEquals("alice", response.assignee());
        assertEquals("Finish backend", response.title());
    }

    @Test
    void shouldUnassignTask() {
        AppUser currentUser = new AppUser();
        currentUser.setId(1L);
        currentUser.setUsername("alice");

        AppUser assignee = new AppUser();
        assignee.setId(2L);
        assignee.setUsername("bob");

        Task task = new Task();
        task.setId(10L);
        task.setTitle("Finish backend");
        task.setDescription("Implement unassign");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setCreatedBy(currentUser);
        task.setAssignee(assignee);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.unassign(10L);

        assertEquals("Finish backend", response.title());
        assertNull(response.assignee());

    }

    @Test
    void shouldAddCommentToTask() {
        AppUser currentUser = new AppUser();
        currentUser.setId(1L);
        currentUser.setUsername("alice");

        Task task = new Task();
        task.setId(10L);
        task.setTitle("Finish backend");

        TaskCommentRequest request = new TaskCommentRequest("Need to clarify acceptance criteria");

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(taskCommentRepository.save(any(TaskComment.class))).thenAnswer(invocation -> {
            TaskComment comment = invocation.getArgument(0);
            comment.setId(100L);
            return comment;
        });

        TaskCommentResponse response = taskService.addComment(10L, request);

        assertEquals(100L, response.id());
        assertEquals("Need to clarify acceptance criteria", response.text());
        assertEquals("alice", response.author());
        assertNotNull(response.createdAt());
    }

    @Test
    void shouldReturnCommentsForTask() {
        Task task = new Task();
        task.setId(10L);
        task.setTitle("Finish backend");

        AppUser alice = new AppUser();
        alice.setId(1L);
        alice.setUsername("alice");

        AppUser bob = new AppUser();
        bob.setId(2L);
        bob.setUsername("bob");

        TaskComment firstComment = new TaskComment();
        firstComment.setId(100L);
        firstComment.setText("Need to clarify acceptance criteria");
        firstComment.setTask(task);
        firstComment.setAuthor(alice);
        firstComment.setCreatedAt(LocalDateTime.of(2025, 3, 25, 10, 0));

        TaskComment secondComment = new TaskComment();
        secondComment.setId(101L);
        secondComment.setText("I will handle this tomorrow");
        secondComment.setTask(task);
        secondComment.setAuthor(bob);
        secondComment.setCreatedAt(LocalDateTime.of(2025, 3, 25, 12, 0));

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskCommentRepository.findAllByTaskIdOrderByCreatedByAsc(10L))
                .thenReturn(List.of(firstComment, secondComment));

    }

}
