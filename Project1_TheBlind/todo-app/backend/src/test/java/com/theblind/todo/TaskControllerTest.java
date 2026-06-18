package com.theblind.todo;

import com.theblind.todo.Controller.TaskController;
import com.theblind.todo.Entity.Task;
import com.theblind.todo.Exception.ResourceNotFoundException;
import com.theblind.todo.Service.TaskService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@DisplayName("TaskController web layer tests")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    // ── helpers ──────────────────────────────────────────────────────────────

    private Task taskWithId(UUID id, String content) {
        Task t = new Task();
        t.setTaskContent(content);
        try {
            var field = Task.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(t, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return t;
    }

    private String taskJson(String content, Boolean isComplete) {
        StringBuilder sb = new StringBuilder("{");
        if (content != null) {
            sb.append("\"taskContent\":\"").append(content).append("\"");
        }
        if (isComplete != null) {
            if (content != null) sb.append(",");
            sb.append("\"isComplete\":").append(isComplete);
        }
        sb.append("}");
        return sb.toString();
    }

    // ── GET /api/tasks ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/tasks")
    class GetAllTasks {

        @Test
        @DisplayName("returns 200 with list of tasks")
        void getAllTasks_returnsList() throws Exception {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            List<Task> tasks = List.of(
                    taskWithId(id1, "Task one"),
                    taskWithId(id2, "Task two")
            );
            when(taskService.getAll()).thenReturn(tasks);

            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].taskContent").value("Task one"))
                    .andExpect(jsonPath("$[1].taskContent").value("Task two"));
        }

        @Test
        @DisplayName("returns 200 with empty array when no tasks exist")
        void getAllTasks_emptyList() throws Exception {
            when(taskService.getAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ── GET /api/tasks/{id} ───────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/tasks/{id}")
    class GetTaskById {

        @Test
        @DisplayName("returns 200 with task when found")
        void getTaskById_found() throws Exception {
            UUID id = UUID.randomUUID();
            Task task = taskWithId(id, "Existing task");
            when(taskService.get(id)).thenReturn(Optional.of(task));

            mockMvc.perform(get("/api/tasks/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskContent").value("Existing task"));
        }

        @Test
        @DisplayName("returns 404 when task does not exist")
        void getTaskById_notFound() throws Exception {
            UUID id = UUID.randomUUID();
            when(taskService.get(id)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/tasks/{id}", id))
                    .andExpect(status().isNotFound());
        }
    }

    // ── POST /api/tasks ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/tasks")
    class CreateTask {

        @Test
        @DisplayName("returns 201 with created task on valid input")
        void createTask_validInput_returns201() throws Exception {
            UUID id = UUID.randomUUID();
            Task saved = taskWithId(id, "New task");
            when(taskService.add(any(Task.class))).thenReturn(saved);

            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(taskJson("New task", null)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.taskContent").value("New task"))
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        @DisplayName("returns 400 when service rejects invalid content")
        void createTask_invalidContent_returns400() throws Exception {
            when(taskService.add(any(Task.class)))
                    .thenThrow(new IllegalArgumentException("Task Content cannot be blank"));

            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(taskJson("", null)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── PATCH /api/tasks/{id} ─────────────────────────────────────────────────

    @Nested
    @DisplayName("PATCH /api/tasks/{id}")
    class UpdateTask {

        @Test
        @DisplayName("returns 200 with updated task when found")
        void updateTask_found_returns200() throws Exception {
            UUID id = UUID.randomUUID();
            Task updated = taskWithId(id, "Updated content");
            updated.setIsComplete(true);
            when(taskService.update(any(Task.class))).thenReturn(Optional.of(updated));

            mockMvc.perform(patch("/api/tasks/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(taskJson("Updated content", true)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.taskContent").value("Updated content"))
                    .andExpect(jsonPath("$.isComplete").value(true));
        }

        @Test
        @DisplayName("returns 404 when task to update does not exist")
        void updateTask_notFound_returns404() throws Exception {
            UUID id = UUID.randomUUID();
            when(taskService.update(any(Task.class))).thenReturn(Optional.empty());

            mockMvc.perform(patch("/api/tasks/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(taskJson("Anything", null)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 400 when service rejects invalid content")
        void updateTask_invalidContent_returns400() throws Exception {
            UUID id = UUID.randomUUID();
            when(taskService.update(any(Task.class)))
                    .thenThrow(new IllegalArgumentException("Text Content cannot be blank or empty."));

            mockMvc.perform(patch("/api/tasks/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(taskJson("", null)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── DELETE /api/tasks/{id} ────────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/tasks/{id}")
    class DeleteTask {

        @Test
        @DisplayName("returns 204 when task is deleted successfully")
        void deleteTask_exists_returns204() throws Exception {
            UUID id = UUID.randomUUID();
            doNothing().when(taskService).delete(id);

            mockMvc.perform(delete("/api/tasks/{id}", id))
                    .andExpect(status().isNoContent());

            verify(taskService).delete(id);
        }

        @Test
        @DisplayName("returns 404 when task does not exist")
        void deleteTask_notFound_returns404() throws Exception {
            UUID id = UUID.randomUUID();
            doThrow(new ResourceNotFoundException("Task not found with id: " + id))
                    .when(taskService).delete(id);

            mockMvc.perform(delete("/api/tasks/{id}", id))
                    .andExpect(status().isNotFound());
        }
    }
}
