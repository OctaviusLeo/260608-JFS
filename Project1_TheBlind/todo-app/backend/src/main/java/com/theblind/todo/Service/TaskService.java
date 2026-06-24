package com.theblind.todo.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.theblind.todo.Entity.Task;
import com.theblind.todo.Repo.TaskRepo;

import com.theblind.todo.Exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepo repo;

   /**
     * Checks content for defined errors.
     *
     * @param content the text content to verify
     * @return an {@link Optional} containing an error message if validation fails, or empty if none are found
     */
    private Optional<String> verifyTextContent(String content) {
        if(content.isBlank()) {
            return Optional.of("Text Content cannot be blank or empty.");
        }

        if(content.length() > 50) {
            return Optional.of("Text Content cannot contain more than 50 characters.");
        }

        return Optional.empty();
    }

   /**
     * Adds a new task to the repository.
     *
     * @param newTask the {@link Task} to add
     * @return the saved {@link Task}
     * @throws IllegalArgumentException if the task content is blank or empty
     */
    
    public Task add(Task newTask) throws IllegalArgumentException {
        if(newTask.getTaskContent().isEmpty()) {
            throw new IllegalArgumentException("Task Content cannot be blank");
        }

        return repo.save(newTask);
    }

   /**
     * Retrieves a task by its unique identifier.
     *
     * @param id the {@link UUID} of the task to retrieve
     * @return an {@link Optional} containing the {@link Task} if found, or empty if not
     */
    public Optional<Task> get(UUID id) {
        return repo.findById(id);
    }

   /**
     * Retrieves all tasks from the repository.
     *
     * @return a {@link List} of all {@link Task} objects
     */
    public List<Task> getAll() {
        return repo.findAll();
    }

   /**
     * Updates an existing task in the repository.
     *
     * @param updatedTask the {@link Task} containing updated fields
     * @return an {@link Optional} containing the updated {@link Task} if found, or empty if no task exists with the given ID
     * @throws IllegalArgumentException if the task content is blank, empty, or exceeds 50 characters
     */
    public Optional<Task> update(Task updatedTask) throws IllegalArgumentException {
        Optional<Task> target = repo.findById(updatedTask.getId());

        if(target.isEmpty()) {
            return target;
        }

        Optional<String> textContentErrors = verifyTextContent(updatedTask.getTaskContent());
        if(textContentErrors.isPresent()) {
            throw new IllegalArgumentException(textContentErrors.get());
        }

        target.get().setIsComplete(updatedTask.getIsComplete());
        target.get().setTaskContent(updatedTask.getTaskContent());

        return Optional.of(repo.save(target.get()));
    }

   /**
     * Deletes a task and its children from the repository.
     *
     * @param id the {@link UUID} of the task to delete
     * @throws ResourceNotFoundException if no task exists with the given ID
     */
    public void delete(UUID id) throws ResourceNotFoundException {
        Optional<Task> target = repo.findById(id);

        if(target.isEmpty()) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }

        // Deleting the entity cascades through the @OneToMany subtask relationship
        // (cascade = ALL, orphanRemoval = true), so the parent and every nested
        // descendant is removed recursively for arbitrarily deep nesting.
        repo.delete(target.get());
    }
}
