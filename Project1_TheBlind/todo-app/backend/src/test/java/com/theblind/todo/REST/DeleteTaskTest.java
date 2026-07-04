package com.theblind.todo.REST;

import com.theblind.todo.Repo.AccountRepo;
import com.theblind.todo.Repo.TaskRepo;

// RESTAssured Team reccommends these imports for ease of framework use
import static io.restassured.RestAssured.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

/**
 * REST Assured API tests for deleting tasks: DELETE /api/tasks/{id}
 *
 * Covers status codes and business logic (cascading deletion of subtasks)
 * against a real embedded server on a random port, backed by the in-memory H2
 * test database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Delete Task RESTAssured API tests")
public class DeleteTaskTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private TaskRepo taskRepo;

    private String token;

    @BeforeEach
    public void setUp() {
        taskRepo.deleteAll();
        accountRepo.deleteAll();

        RestAssured.baseURI = "http://localhost/api";
        RestAssured.port = port;

        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"john_doe\",\"password\":\"AbcDe**123\"}")
        .when()
            .post("/register")
        .then()
            .statusCode(201);

        token = obtainToken();
    }

    private String obtainToken() {
        return given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"john_doe\",\"password\":\"AbcDe**123\"}")
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .extract().path("token");
    }

    /**
     * Creates a top-level task and returns its generated id.
     */
    private String createTask(String content) {
        return given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"taskContent\":\"" + content + "\"}")
        .when()
            .post("/tasks")
        .then()
            .statusCode(201)
            .extract().path("id");
    }

    /**
     * Creates a subtask under the given parent and returns its generated id.
     */
    private String createSubtask(String content, String parentId) {
        return given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"taskContent\":\"" + content + "\",\"parent_task_id\":\"" + parentId + "\"}")
        .when()
            .post("/tasks")
        .then()
            .statusCode(201)
            .extract().path("id");
    }

    /**
     * TEST 1
     * DELETE /api/tasks/{id} for an existing task.
     *
     * Expected: 204 No Content, and a subsequent GET returns 404.
     */
    @Test
    @DisplayName("Successful delete - returns 204 and task is gone")
    void deleteTaskSuccess() {
        String id = createTask("Task to delete");

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/tasks/" + id)
        .then()
            .statusCode(204);

        // The task should no longer be retrievable.
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/tasks/" + id)
        .then()
            .statusCode(404);
    }

    /**
     * TEST 2
     * DELETE /api/tasks/{id} for a parent task with a subtask.
     *
     * Expected: 204 No Content, and the subtask is cascade-deleted (GET returns 404).
     */
    @Test
    @DisplayName("Successful delete - subtasks are cascade deleted")
    void deleteTaskCascadesToSubtasks() {
        String parentId = createTask("Parent task");
        String childId = createSubtask("Child task", parentId);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/tasks/" + parentId)
        .then()
            .statusCode(204);

        // The child task must be removed along with its parent.
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/tasks/" + childId)
        .then()
            .statusCode(404);
    }

    /**
     * TEST 3
     * DELETE /api/tasks/{id} for a task id that does not exist.
     *
     * Expected: 404 Not Found.
     */
    @Test
    @DisplayName("Unsuccessful delete - task does not exist")
    void deleteTaskNotFound() {
        UUID randomId = UUID.randomUUID();

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/tasks/" + randomId)
        .then()
            .statusCode(404);
    }

    /**
     * TEST 4
     * DELETE /api/tasks/{id} without an Authorization header.
     *
     * Expected: 401 Unauthorized (protected endpoint).
     */
    @Test
    @DisplayName("Unsuccessful delete - missing JWT token")
    void deleteTaskWithoutToken() {
        String id = createTask("Task to delete");

        given()
        .when()
            .delete("/tasks/" + id)
        .then()
            .statusCode(401);
    }
}
