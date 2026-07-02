package com.theblind.todo.REST;

import com.theblind.todo.Repo.AccountRepo;
import com.theblind.todo.Repo.TaskRepo;

// RESTAssured Team reccommends these imports for ease of framework use
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

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
 * REST Assured API tests for updating tasks: PATCH /api/tasks/{id}
 *
 * Covers status codes, payload integrity, and business logic (content and
 * completion-state changes) against a real embedded server on a random port,
 * backed by the in-memory H2 test database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Update Task RESTAssured API tests")
public class UpdateTaskTest {

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
     * Creates a task with the given content and returns its generated id so the
     * update tests have a real, owned target to modify.
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
     * TEST 1
     * PATCH /api/tasks/{id} changing the task content.
     *
     * Expected: 200 OK, body reflects the new content while the id is unchanged.
     */
    @Test
    @DisplayName("Successful update - task content is changed")
    void updateTaskContentSuccess() {
        String id = createTask("Original content");

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"taskContent\":\"Updated content\"}")
        .when()
            .patch("/tasks/" + id)
        .then()
            .statusCode(200)
            .body("id", equalTo(id))
            .body("taskContent", equalTo("Updated content"));
    }

    /**
     * TEST 2
     * PATCH /api/tasks/{id} marking the task complete.
     *
     * Expected: 200 OK, isComplete flips to true.
     */
    @Test
    @DisplayName("Successful update - task marked complete")
    void updateTaskMarkComplete() {
        String id = createTask("Finish report");

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"taskContent\":\"Finish report\",\"isComplete\":true}")
        .when()
            .patch("/tasks/" + id)
        .then()
            .statusCode(200)
            .body("isComplete", equalTo(true));
    }

    /**
     * TEST 3
     * PATCH /api/tasks/{id} for a task id that does not exist.
     *
     * Expected: 404 Not Found.
     */
    @Test
    @DisplayName("Unsuccessful update - task does not exist")
    void updateTaskNotFound() {
        UUID randomId = UUID.randomUUID();

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"taskContent\":\"Does not matter\"}")
        .when()
            .patch("/tasks/" + randomId)
        .then()
            .statusCode(404);
    }

    /**
     * TEST 4
     * PATCH /api/tasks/{id} with blank content on an existing task.
     *
     * Expected: 400 Bad Request (content cannot be blank).
     */
    @Test
    @DisplayName("Unsuccessful update - blank content")
    void updateTaskBlankContent() {
        String id = createTask("Valid content");

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"taskContent\":\"\"}")
        .when()
            .patch("/tasks/" + id)
        .then()
            .statusCode(400);
    }

    /**
     * TEST 5
     * PATCH /api/tasks/{id} with content above the 50-character maximum.
     *
     * Expected: 400 Bad Request.
     */
    @Test
    @DisplayName("Unsuccessful update - content above maximum length")
    void updateTaskContentTooLong() {
        String id = createTask("Valid content");
        String tooLong = "a".repeat(51);

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"taskContent\":\"" + tooLong + "\"}")
        .when()
            .patch("/tasks/" + id)
        .then()
            .statusCode(400);
    }

    /**
     * TEST 6
     * PATCH /api/tasks/{id} without an Authorization header.
     *
     * Expected: 401 Unauthorized (protected endpoint).
     */
    @Test
    @DisplayName("Unsuccessful update - missing JWT token")
    void updateTaskWithoutToken() {
        String id = createTask("Valid content");

        given()
            .contentType(ContentType.JSON)
            .body("{\"taskContent\":\"Updated content\"}")
        .when()
            .patch("/tasks/" + id)
        .then()
            .statusCode(401);
    }
}
