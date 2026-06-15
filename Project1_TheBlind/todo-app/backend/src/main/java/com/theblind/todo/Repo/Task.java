package com.theblind.todo.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.theblind.todo.Entity.tasks;

@Repository
public interface Task extends JpaRepository<tasks, Integer> {
    List<tasks> findByUserId(Integer userId);
    List<tasks> findByParentTaskId(Integer parentTaskId);
}
