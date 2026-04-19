package com.tasktracker.task.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tasktracker.auth.api.LoginRequest;
import com.tasktracker.auth.api.RegisterRequest;
import com.tasktracker.auth.api.UserRoleUpdateRequest;
import com.tasktracker.auth.domain.Role;
import com.tasktracker.auth.repository.UserRepository;
import com.tasktracker.support.PostgresIntegrationTest;
import com.tasktracker.task.domain.TaskPriority;
import com.tasktracker.task.domain.TaskStatus;
import com.tasktracker.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class TaskControllerTest extends PostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        taskRepository.deleteAll();
        userRepository.findByUsername("alice").ifPresent(userRepository::delete);
        userRepository.findByUsername("bob").ifPresent(userRepository::delete);
        userRepository.findByUsername("weak-user").ifPresent(userRepository::delete);
    }

    @Test
    void shouldRegisterLoginAndReadCurrentUser() throws Exception {
        String token = register("alice", "Password123!");

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.role").value("USER"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("alice", "Password123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldCreateFilterAndPatchTask() throws Exception {
        String aliceToken = register("alice", "Password123!");
        register("bob", "Password123!");
        String adminToken = login("admin", "admin12345");

        TaskRequest request = new TaskRequest(
                "Finish task tracker",
                "Add JWT auth and assignment",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                LocalDateTime.of(2025, 3, 30, 18, 0),
                "alice"
        );

        String createdTask = mockMvc.perform(post("/api/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Finish task tracker"))
                .andExpect(jsonPath("$.createdBy").value("alice"))
                .andExpect(jsonPath("$.assignee").value("alice"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(get("/api/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(aliceToken))
                        .param("status", "TODO")
                        .param("priority", "HIGH")
                        .param("q", "JWT")
                        .param("createdBy", "alice")
                        .param("assignee", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].createdBy").value("alice"));

        Long taskId = readId(createdTask);
        TaskPatchRequest adminPatch = new TaskPatchRequest(
                null,
                null,
                TaskStatus.IN_PROGRESS,
                null,
                null,
                "bob"
        );

        mockMvc.perform(patch("/api/tasks/{id}", taskId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminPatch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.assignee").value("bob"));
    }

    @Test
    void shouldRejectUnauthorizedAndForbiddenOperations() throws Exception {
        String aliceToken = register("alice", "Password123!");
        String bobToken = register("bob", "Password123!");

        TaskRequest request = new TaskRequest(
                "Owner task",
                "Owned by alice",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                null,
                "alice"
        );

        String createdTask = mockMvc.perform(post("/api/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long taskId = readId(createdTask);

        TaskPatchRequest bobPatch = new TaskPatchRequest(
                "Hacked",
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(patch("/api/tasks/{id}", taskId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bobPatch)))
                .andExpect(status().isForbidden());

        TaskRequest assignOtherUser = new TaskRequest(
                "Illegal assignment",
                "User tries to assign another user",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                null,
                "bob"
        );

        mockMvc.perform(post("/api/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignOtherUser)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/tasks/{id}", taskId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(bobToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToDeleteAnyTask() throws Exception {
        String aliceToken = register("alice", "Password123!");
        String adminToken = login("admin", "admin12345");

        TaskRequest request = new TaskRequest(
                "Admin can delete",
                "Role based access",
                TaskStatus.TODO,
                TaskPriority.LOW,
                null,
                "alice"
        );

        String createdTask = mockMvc.perform(post("/api/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long taskId = readId(createdTask);

        mockMvc.perform(delete("/api/tasks/{id}", taskId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldValidateStrongPasswordAndManageUsers() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest("weak-user", "weakpass"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        String aliceToken = register("alice", "Password123!");
        String adminToken = login("admin", "admin12345");
        register("bob", "Password123!");
        Long bobId = findUserIdByUsername("bob");

        mockMvc.perform(get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(aliceToken))
                        .param("q", "bo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("bob"));

        mockMvc.perform(get("/api/users/{id}", bobId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(aliceToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"))
                .andExpect(jsonPath("$.role").value("USER"));

        mockMvc.perform(patch("/api/users/{id}/role", bobId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserRoleUpdateRequest(Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mockMvc.perform(patch("/api/users/{id}/role", bobId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserRoleUpdateRequest(Role.USER))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFilterTasksByDueDateFrom() throws Exception {
        String aliceToken = register("alice", "Password123");

        TaskRequest first = new TaskRequest(
                "Old task",
                "Should be filtered out",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                LocalDateTime.of(2025, 3, 10, 10, 0),
                "alice"
        );

        TaskRequest second = new TaskRequest(
                "New task",
                "Should remain in result",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                LocalDateTime.of(2025, 3, 30, 10, 0),
                "alice"
        );

        mockMvc.perform(post("/api/tasks")
                .header(HttpHeaders.AUTHORIZATION, bearer(aliceToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks")
                .header(HttpHeaders.AUTHORIZATION, bearer(aliceToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks")
                .header(HttpHeaders.AUTHORIZATION, bearer(aliceToken))
                .param("dueDateFrom", "2025-03-20T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("New Task"));

    }

    private String register(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(username, password))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return readField(response, "token");
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return readField(response, "token");
    }

    private Long readId(String json) throws Exception {
        return objectMapper.readTree(json).get("id").asLong();
    }

    private String readField(String json, String field) throws Exception {
        JsonNode node = objectMapper.readTree(json);
        return node.get(field).asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private Long findUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow()
                .getId();
    }
}
