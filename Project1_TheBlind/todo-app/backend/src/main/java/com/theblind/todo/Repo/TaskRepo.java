package com.theblind.todo.Repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.theblind.todo.Entity.Task;

import jakarta.transaction.Transactional;

@Repository
public interface TaskRepo extends JpaRepository<Task, UUID> {
    List<Task> findByUserId(UUID userId);
    List<Task> findByParentTaskId(Integer parentTaskId);

    @Transactional
    @Modifying
    @Query(
        nativeQuery = true,
        value = """
                WITH RECURSIVE Children AS (
                    SELECT id FROM Task WHERE id = :id

                    UNION ALL

                    SELECT child.id
                    FROM Task child
                    INNER JOIN Children parent ON child.parent_task_id = parent.id
                )
                
                DELETE FROM Task WHERE id IN Children
                """
    )
    void deleteChildren(UUID id);
}
