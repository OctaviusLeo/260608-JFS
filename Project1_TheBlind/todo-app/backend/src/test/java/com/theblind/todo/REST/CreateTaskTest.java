package com.theblind.todo.REST;

import com.theblind.todo.Repo.AccountRepo;
import com.theblind.todo.Repo.TaskRepo;

// RESTAssured Team reccommends these imports for ease of framework use
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

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
 * REST Assured API tests for creating tasks: POST /api/tasks
 *
 * Covers status codes, payload integrity, and business logic (subtask creation)
 * against a real embedded server on a random port, backed by the in-memory H2
 * test database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Create Task RESTAssured API tests")
public class CreateTaskTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private TaskRepo taskRepo;

    // JWT bearer token for the authenticated user, refreshed before each test.
    private String token;

    @BeforeEach
    public void setUp() {
        // Wipe tasks before accounts so no orphaned rows survive between tests.
        taskRepo.deleteAll();
        accountRepo.deleteAll();

        RestAssured.baseURI = "http://localhost/api";
        RestAssured.port = port;

        // Register the user the task endpoints will operate on. A non-201 here
        // means the setup is broken, so we assert on it to fail fast.
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"john_doe\",\"password\":\"AbcDe**123\"}")
        .when()
            .post("/register")
        .then()
            .statusCode(201);

        token = obtainToken();
    }

    /**
     * Logs in the registered user and extracts the JWT so protected task
     * endpoints can be exercised with a valid Authorization header.
     */
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
     * TEST 1
     * POST /api/tasks with a valid content and a valid token.
     *
     * Expected: 201 Created, body echoes taskContent, has a generated id,
     * and defaults isComplete to false.
     */
    @Test
    @DisplayName("Successful task creation - returns 201 and created task")
    void createTaskSuccess() {
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"taskContent\":\"Buy groceries\"}")
        .when()
            .post("/tasks")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("taskContent", equalTo("Buy groceries"))
            .body("isComplete", equalTo(false));
    }

    /**
     * TEST 2
     * POST /api/tasks with content exactly at the 50-character maximum.
     *
     * Expected: 201 Created.
     */
    @Test
    @DisplayName("Successful task creation - content at maximum length (50 chars)")
    void createTaskMaxLength() {
        String maxContent = "a".repeat(50);

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"taskContent\":\"" + maxContent + "\"}")
        .when()
            .post("/tasks")
        .then()
            .statusCode(201)
            .body("taskContent", equalTo(maxContent));
    }

    /**
     * TEST 3
     * POST /api/tasks with a subtask referencing an existing parent task.
     *
     * Expected: 201 Created and the parent_task_id is persisted on the subtask,
     * verifying the hierarchical-task business logic.
     */
    @Test
    @DisplayName("Successful subtask creation - parent_task_id is persisted")
    void createSubtaskSuccess() {
        String parentId =
            given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("{\"taskContent\":\"Parent task\"}")
            .when()
                .post("/tasks")
            .then()
                .statusCode(201)
                .extract().path("id");

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"taskContent\":\"Child task\",\"parent_task_id\":\"" + parentId + "\"}")
        .when()
            .post("/tasks")
        .then()
            .statusCode(201)
            .body("taskContent", equalTo("Child task"))
            .body("parent_task_id", equalTo(parentId));
    }

    /**
     * TEST 4
     * POST /api/tasks with blank content.
     *
     * Expected: 400 Bad Request (content cannot be blank).
     */
    @Test
    @DisplayName("Unsuccessful task creation - blank content")
    void createTaskBlankContent() {
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"taskContent\":\"\"}")
        .when()
            .post("/tasks")
        .then()
            .statusCode(400);
    }

    /**
     * TEST 5
     * POST /api/tasks with content above the 50-character maximum.
     *
     * Expected: 400 Bad Request.
     */
    @Test
    @DisplayName("Unsuccessful task creation - content above maximum length")
    void createTaskContentTooLong() {
        String tooLong = "a".repeat(51);

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"taskContent\":\"" + tooLong + "\"}")
        .when()
            .post("/tasks")
        .then()
            .statusCode(400);
    }

    /**
     * TEST 6
     * POST /api/tasks without an Authorization header.
     *
     * Expected: 401 Unauthorized (protected endpoint).
     */
    @Test
    @DisplayName("Unsuccessful task creation - missing JWT token")
    void createTaskWithoutToken() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"taskContent\":\"No auth\"}")
        .when()
            .post("/tasks")
        .then()
            .statusCode(401);
    }
}
