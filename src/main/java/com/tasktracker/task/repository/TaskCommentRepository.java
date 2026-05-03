package com.tasktracker.task.repository;

import com.tasktracker.task.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {

    List<TaskComment> findAllByTaskIdOrderByCreatedByAsc(Long taskId);
}
