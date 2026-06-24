package com.theblind.todo.Controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.theblind.todo.Entity.Task;
import com.theblind.todo.Service.TaskService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final String requestOriginURL = "http://localhost:4200";

    /**
     * Retrieve all tasks.
     *
     * @return ResponseEntity containing a list of all {@link Task} objects with HTTP status 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAll());
    }

    /**
     * Create a new task.
     *
     * @param task the task to create (bound from the request body)
     * @return ResponseEntity containing the created {@link Task} with HTTP status 201 (Created).
     * @throws IllegalArgumentException if the task input is invalid (handled by GlobalExceptionHandler)
     */
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task created = taskService.add(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Retrieve a task by its id.
     *
     * @param id the task id
     * @return ResponseEntity containing the {@link Task} and HTTP status 200 (OK) if found,
     *         or HTTP status 404 (Not Found) if the task does not exist.
     */
    @CrossOrigin(origins = requestOriginURL)
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable UUID id) {
        return taskService.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update an existing task.
     *
     * @param id the id of the task to update
     * @param task the task data to update (bound from the request body); the id will be set to the provided id
     * @return ResponseEntity containing the updated {@link Task} with HTTP status 200 (OK) if found,
     *         or HTTP status 404 (Not Found) if no task with the given id exists.
     * @throws IllegalArgumentException if the provided input is invalid (handled by GlobalExceptionHandler)
     */
    @CrossOrigin(origins = requestOriginURL)
    @PatchMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable UUID id, @RequestBody Task task) {
        task.setId(id);
        return taskService.update(task)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a task by its id.
     *
     * @param id the id of the task to delete
     * @return ResponseEntity with HTTP status 204 (No Content) when deletion succeeds.
     * @throws EntityNotFoundException if the task does not exist (handled by GlobalExceptionHandler)
     */
    @CrossOrigin(origins = requestOriginURL)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
}
