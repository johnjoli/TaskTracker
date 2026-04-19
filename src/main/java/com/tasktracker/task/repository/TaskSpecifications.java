package com.tasktracker.task.repository;

import com.tasktracker.task.domain.Task;
import com.tasktracker.task.domain.TaskPriority;
import com.tasktracker.task.domain.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class TaskSpecifications {

    private TaskSpecifications() {
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(TaskPriority priority) {
        return (root, query, criteriaBuilder) ->
                priority == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("priority"), priority);
    }

    public static Specification<Task> titleOrDescriptionContains(String queryText) {
        return (root, query, criteriaBuilder) -> {
            if (queryText == null || queryText.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%" + queryText.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Task> hasDueDateFrom(LocalDateTime dueDateFrom) {
        return (root, query, cb) ->
                dueDateFrom == null ? null : cb.greaterThanOrEqualTo(root.get("dueDate"), dueDateFrom);
    }

    public static Specification<Task> hasDueDateTo (LocalDateTime dueDateTo) {
        return (root, query, cb) ->
                dueDateTo == null ? null : cb.lessThanOrEqualTo(root.get("dueDate"), dueDateTo);
    }

    public static Specification<Task> hasCreatedBy(String username) {
        return (root, query, criteriaBuilder) ->
                username == null || username.isBlank()
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("createdBy").get("username"), username);
    }

    public static Specification<Task> hasAssignee(String username) {
        return (root, query, criteriaBuilder) ->
                username == null || username.isBlank()
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("assignee").get("username"), username);
    }
}
