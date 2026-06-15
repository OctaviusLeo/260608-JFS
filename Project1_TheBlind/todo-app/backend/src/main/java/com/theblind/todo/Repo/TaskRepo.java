package com.theblind.todo.Repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.theblind.todo.Entity.Task;

@Repository
public interface TaskRepo extends JpaRepository<Task, UUID> {
    List<Task> findByUserId(UUID userId);
    List<Task> findByParentTaskId(Integer parentTaskId);
}
