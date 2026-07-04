package com.theblind.todo.e2e;

import com.theblind.todo.Repo.AccountRepo;
import com.theblind.todo.Repo.TaskRepo;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import static io.restassured.RestAssured.given;

// Helper used by step definitions to set up and clean up test data via the REST API.
// Inject this with @Autowired in any step class.
// The server port is read from Environment at call-time to avoid issues during Spring startup.
@Service
public class TestDataHelper {

    @Autowired
    private Environment environment;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private TaskRepo taskRepo;

    // Returns the port the test server is running on
    public int getPort() {
        return Integer.parseInt(environment.getProperty("local.server.port", "8080"));
    }

    // Points REST-Assured at the running test server — call this before making requests
    public void configureRestAssured() {
        RestAssured.baseURI = "http://localhost/api";
        RestAssured.port    = getPort();
    }

    // Wipes all tasks then all accounts — call in a @Before hook to start clean
    public void cleanDatabase() {
        taskRepo.deleteAll();
        accountRepo.deleteAll();
    }

    // Registers a user — expects 201
    public void registerUser(String username, String password) {
        configureRestAssured();
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
        .when()
            .post("/register")
        .then()
            .statusCode(201);
    }

    // Logs in and returns the JWT token
    public String loginUser(String username, String password) {
        configureRestAssured();
        return given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .extract()
            .path("token");
    }

    // Creates a top-level task and returns its UUID
    public String createTask(String jwt, String content) {
        configureRestAssured();
        return given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + jwt)
            .body("{\"taskContent\":\"" + content + "\"}")
        .when()
            .post("/tasks")
        .then()
            .statusCode(201)
            .extract()
            .path("id");
    }

    // Creates a subtask under the given parent and returns its UUID
    public String createSubtask(String jwt, String content, String parentId) {
        configureRestAssured();
        return given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + jwt)
            .body("{\"taskContent\":\"" + content + "\",\"parent_task_id\":\"" + parentId + "\"}")
        .when()
            .post("/tasks")
        .then()
            .statusCode(201)
            .extract()
            .path("id");
    }

    // Deletes a task by ID — expects 204
    public void deleteTask(String jwt, String id) {
        configureRestAssured();
        given()
            .header("Authorization", "Bearer " + jwt)
        .when()
            .delete("/tasks/" + id)
        .then()
            .statusCode(204);
    }
}
