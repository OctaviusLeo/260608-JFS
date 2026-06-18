package com.theblind.todo;

import com.theblind.todo.Entity.Task;
import com.theblind.todo.Exception.ResourceNotFoundException;
import com.theblind.todo.Repo.TaskRepo;
import com.theblind.todo.Service.TaskService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService unit tests")
class TaskServiceTest {

    @Mock
    private TaskRepo repo;

    @InjectMocks
    private TaskService service;

    // ── helpers ──────────────────────────────────────────────────────────────

    private Task taskWith(String content) {
        Task t = new Task();
        t.setTaskContent(content);
        return t;
    }

    private Task taskWithId(UUID id, String content) {
        Task t = taskWith(content);
        // Simulate a persisted task that already carries an id.
        // We set it via reflection so the test doesn't depend on a setter
        // that the entity doesn't expose.
        try {
            var field = Task.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(t, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return t;
    }

    // ── add() ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("add()")
    class Add {

        @Test
        @DisplayName("saves and returns task when content is valid")
        void add_validContent_returnsTask() {
            Task input = taskWith("Buy groceries");
            Task saved = taskWith("Buy groceries");
            when(repo.save(input)).thenReturn(saved);

            Task result = service.add(input);

            assertThat(result).isSameAs(saved);
            verify(repo).save(input);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when content is empty")
        void add_emptyContent_throws() {
            Task input = taskWith("");

            assertThatThrownBy(() -> service.add(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("blank");

            verifyNoInteractions(repo);
        }
    }

    // ── get() ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("get()")
    class Get {

        @Test
        @DisplayName("returns task wrapped in Optional when found")
        void get_existingId_returnsOptional() {
            UUID id = UUID.randomUUID();
            Task task = taskWithId(id, "Existing task");
            when(repo.findById(id)).thenReturn(Optional.of(task));

            Optional<Task> result = service.get(id);

            assertThat(result).isPresent().contains(task);
        }

        @Test
        @DisplayName("returns empty Optional when task does not exist")
        void get_unknownId_returnsEmpty() {
            UUID id = UUID.randomUUID();
            when(repo.findById(id)).thenReturn(Optional.empty());

            Optional<Task> result = service.get(id);

            assertThat(result).isEmpty();
        }
    }

    // ── getAll() ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("returns all tasks from the repository")
        void getAll_returnsList() {
            List<Task> tasks = List.of(taskWith("A"), taskWith("B"));
            when(repo.findAll()).thenReturn(tasks);

            List<Task> result = service.getAll();

            assertThat(result).hasSize(2).containsExactlyElementsOf(tasks);
        }

        @Test
        @DisplayName("returns empty list when repository is empty")
        void getAll_emptyRepo_returnsEmptyList() {
            when(repo.findAll()).thenReturn(List.of());

            assertThat(service.getAll()).isEmpty();
        }
    }

    // ── update() ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("update()")
    class Update {

        private UUID id;
        private Task existing;

        @BeforeEach
        void setUp() {
            id = UUID.randomUUID();
            existing = taskWithId(id, "Old content");
            existing.setIsComplete(false);
        }

        @Test
        @DisplayName("updates content and completion flag on a valid task")
        void update_validTask_returnsUpdated() {
            Task updated = taskWithId(id, "New content");
            updated.setIsComplete(true);

            when(repo.findById(id)).thenReturn(Optional.of(existing));
            when(repo.save(existing)).thenReturn(existing);

            Optional<Task> result = service.update(updated);

            assertThat(result).isPresent();
            assertThat(existing.getTaskContent()).isEqualTo("New content");
            assertThat(existing.getIsComplete()).isTrue();
            verify(repo).save(existing);
        }

        @Test
        @DisplayName("returns empty Optional when task id is not found")
        void update_unknownId_returnsEmpty() {
            Task updated = taskWithId(id, "Anything");
            when(repo.findById(id)).thenReturn(Optional.empty());

            Optional<Task> result = service.update(updated);

            assertThat(result).isEmpty();
            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("throws IllegalArgumentException when updated content is empty")
        void update_emptyContent_throws() {
            Task updated = taskWithId(id, "");
            when(repo.findById(id)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> service.update(updated))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("blank or empty");

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("throws IllegalArgumentException when updated content exceeds 50 characters")
        void update_contentTooLong_throws() {
            String longContent = "A".repeat(51);
            Task updated = taskWithId(id, longContent);
            when(repo.findById(id)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> service.update(updated))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("50 characters");

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("accepts content that is exactly 50 characters (boundary)")
        void update_contentExactly50Chars_succeeds() {
            String boundary = "A".repeat(50);
            Task updated = taskWithId(id, boundary);
            when(repo.findById(id)).thenReturn(Optional.of(existing));
            when(repo.save(existing)).thenReturn(existing);

            assertThatNoException().isThrownBy(() -> service.update(updated));
            verify(repo).save(existing);
        }
    }

    // ── delete() ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("calls deleteChildren when task exists")
        void delete_existingId_callsDeleteChildren() {
            UUID id = UUID.randomUUID();
            Task task = taskWithId(id, "To delete");
            when(repo.findById(id)).thenReturn(Optional.of(task));
            doNothing().when(repo).deleteChildren(id);

            assertThatNoException().isThrownBy(() -> service.delete(id));

            verify(repo).deleteChildren(id);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when task does not exist")
        void delete_unknownId_throws() {
            UUID id = UUID.randomUUID();
            when(repo.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(id.toString());

            verify(repo, never()).deleteChildren(any());
        }
    }
}
