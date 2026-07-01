package com.theblind.todo.REST;

import com.theblind.todo.Repo.AccountRepo;
import com.theblind.todo.Repo.TaskRepo;

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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Task GET endpoints - RESTAssured tests")
public class TaskGetTest {

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

        // Register a user
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"task_user\",\"password\":\"AbcDe**123\"}")
        .when()
            .post("/register")
        .then()
            .statusCode(201);

        // Login and extract JWT token
        token = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"task_user\",\"password\":\"AbcDe**123\"}")
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .extract()
            .path("token");

        // Create a couple of tasks for the authenticated user
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("{\"taskContent\":\"Buy groceries\"}")
        .when()
            .post("/tasks")
        .then()
            .statusCode(201);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("{\"taskContent\":\"Walk the dog\"}")
        .when()
            .post("/tasks")
        .then()
            .statusCode(201);
    }

    // ========================
    // GET /api/tasks
    // ========================

    /**
     * TEST 1 - Happy path
     * GET /api/tasks with valid JWT returns 200 and a list of all tasks.
     */
    @Test
    @DisplayName("GET /api/tasks - authenticated user receives all tasks (200)")
    void getAllTasks_authenticated_returns200() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/tasks")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", org.hamcrest.Matchers.equalTo(2));
    }

    /**
     * TEST 2 - Sad path
     * GET /api/tasks without JWT returns 401 Unauthorized.
     */
    @Test
    @DisplayName("GET /api/tasks - unauthenticated request returns 401")
    void getAllTasks_unauthenticated_returns401() {
        given()
        .when()
            .get("/tasks")
        .then()
            .statusCode(401);
    }

    /**
     * TEST 3 - Sad path
     * GET /api/tasks with an invalid/expired token returns 401.
     */
    @Test
    @DisplayName("GET /api/tasks - invalid token returns 401")
    void getAllTasks_invalidToken_returns401() {
        given()
            .header("Authorization", "Bearer invalid.token.value")
        .when()
            .get("/tasks")
        .then()
            .statusCode(401);
    }

    // ========================
    // GET /api/tasks/{id}
    // ========================

    /**
     * TEST 4 - Happy Path
     * GET /api/tasks/{id} with valid JWT and existing task returns 200.
     */
    @Test
    @DisplayName("GET /api/tasks/{id} - existing task returns 200 with task data")
    void getTaskById_existingTask_returns200() {
        // Create a task and capture its id
        String taskId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("{\"taskContent\":\"Specific task\"}")
        .when()
            .post("/tasks")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/tasks/" + taskId)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("taskContent", org.hamcrest.Matchers.equalTo("Specific task"))
            .body("id", org.hamcrest.Matchers.equalTo(taskId));
    }

    /**
     * TEST 5 - Sad Path
     * GET /api/tasks/{id} with valid JWT but non-existent id returns 404.
     */
    @Test
    @DisplayName("GET /api/tasks/{id} - non-existent task returns 404")
    void getTaskById_nonExistentTask_returns404() {
        String randomId = UUID.randomUUID().toString();

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/tasks/" + randomId)
        .then()
            .statusCode(404);
    }

    /**
     * TEST 6 - Sad path
     * GET /api/tasks/{id} without JWT returns 401.
     */
    @Test
    @DisplayName("GET /api/tasks/{id} - unauthenticated request returns 401")
    void getTaskById_unauthenticated_returns401() {
        given()
        .when()
            .get("/tasks/" + UUID.randomUUID().toString())
        .then()
            .statusCode(401);
    }

    /**
     * TEST 7 - Sad path
     * GET /api/tasks/{id} with an invalid token returns 401.
     */
    @Test
    @DisplayName("GET /api/tasks/{id} - invalid token returns 401")
    void getTaskById_invalidToken_returns401() {
        given()
            .header("Authorization", "Bearer bad.token.here")
        .when()
            .get("/tasks/" + UUID.randomUUID().toString())
        .then()
            .statusCode(401);
    }

    // ========================
    // GET /api/tasks/current_user
    // ========================

    /**
     * TEST 8 - Happy path
     * GET /api/tasks/current_user with valid JWT returns 200 and only the current user's tasks.
     */
    @Test
    @DisplayName("GET /api/tasks/current_user - returns only current user's tasks (200)")
    void getTasksByCurrentUser_authenticated_returns200() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/tasks/current_user")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", org.hamcrest.Matchers.equalTo(2));
    }

    /**
     * TEST 9 - Sad path
     * GET /api/tasks/current_user returns only the authenticated user's tasks, not tasks from other users.
     */
    @Test
    @DisplayName("GET /api/tasks/current_user - does not include another user's tasks")
    void getTasksByCurrentUser_excludesOtherUserTasks() {
        // Register and login a second user
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"other_user\",\"password\":\"AbcDe**123\"}")
        .when()
            .post("/register")
        .then()
            .statusCode(201);

        String otherToken = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"other_user\",\"password\":\"AbcDe**123\"}")
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .extract()
            .path("token");

        // Create a task for the second user
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + otherToken)
            .body("{\"taskContent\":\"Other user's task\"}")
        .when()
            .post("/tasks")
        .then()
            .statusCode(201);

        // The original user should still only see their own 2 tasks
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/tasks/current_user")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", org.hamcrest.Matchers.equalTo(2));

        // The second user should only see their 1 task
        given()
            .header("Authorization", "Bearer " + otherToken)
        .when()
            .get("/tasks/current_user")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", org.hamcrest.Matchers.equalTo(1));
    }

    /**
     * TEST 10 - Sad path
     * GET /api/tasks/current_user without JWT returns 401.
     */
    @Test
    @DisplayName("GET /api/tasks/current_user - unauthenticated request returns 401")
    void getTasksByCurrentUser_unauthenticated_returns401() {
        given()
        .when()
            .get("/tasks/current_user")
        .then()
            .statusCode(401);
    }

    /**
     * TEST 11 - Sad path
     * GET /api/tasks/current_user with invalid token returns 401.
     */
    @Test
    @DisplayName("GET /api/tasks/current_user - invalid token returns 401")
    void getTasksByCurrentUser_invalidToken_returns401() {
        given()
            .header("Authorization", "Bearer invalid.jwt.token")
        .when()
            .get("/tasks/current_user")
        .then()
            .statusCode(401);
    }

    /**
     * TEST 12 - Happy path
     * GET /api/tasks/current_user for a user with no tasks returns 200 with empty list.
     */
    @Test
    @DisplayName("GET /api/tasks/current_user - user with no tasks returns empty list (200)")
    void getTasksByCurrentUser_noTasks_returnsEmptyList() {
        // Register and login a fresh user with no tasks
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"empty_user\",\"password\":\"AbcDe**123\"}")
        .when()
            .post("/register")
        .then()
            .statusCode(201);

        String emptyToken = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"empty_user\",\"password\":\"AbcDe**123\"}")
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .extract()
            .path("token");

        given()
            .header("Authorization", "Bearer " + emptyToken)
        .when()
            .get("/tasks/current_user")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", org.hamcrest.Matchers.equalTo(0));
    }
}
