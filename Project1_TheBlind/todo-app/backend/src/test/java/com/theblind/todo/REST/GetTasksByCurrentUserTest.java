package com.theblind.todo.REST;

import com.theblind.todo.Entity.User;
import com.theblind.todo.Repo.AccountRepo;
import com.theblind.todo.Entity.Task;
import com.theblind.todo.Repo.TaskRepo;

// RESTAssured Team reccommends these imports for ease of framework use
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.TestPropertySource;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

// tells app that, during testing, for this class, the web environment is actually real
// real and during testing, it  is listening on a random port
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// overrides default properties file
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Get all tasks from currently logged in user RESTAssured unit tests")
public class GetTasksByCurrentUserTest {
    // Port in variable
    @LocalServerPort
    private int port;

    // Injecting the repos directly lets us wipe the database before each test,
    // guaranteeing a clean slate regardless of test order or context reuse.
    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private TaskRepo taskRepo;

    private String[] tokens = new String[2];
    private User[] users = new User[2];

    // before each test, create two users, log them in, have them make two tasks each, then log them out
    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "http://localhost/api";
        RestAssured.port = port;

        taskRepo.deleteAll();
        accountRepo.deleteAll();

        User testUser1 = new User();
        testUser1.setUsername("john_doe");
        testUser1.setPassword("Abc**4");

        User testUser2 = new User();
        testUser2.setUsername("jane_doe");
        testUser2.setPassword("Abc**4");

        this.users[0] = testUser1;
        this.users[1] = testUser2;
        String token = null;
        String[][] taskContents = {{"Hello", "World"}, {"FOO", "BAR"}};
        
        for (int i = 0; i < this.users.length; i++) {
            // create new user
            given().
                contentType(ContentType.JSON).
                body(users[i]).
                when().
                    post("/register").
                then().
                    statusCode(201);

            // log user in a get token from body
            token = given().
                contentType(ContentType.JSON).
                body(this.users[i]).
                when().
                    post("/auth/login").
                then().
                    statusCode(200).
                    extract().
                    path("token").
                    toString();

            // after login, use token to authenticate user and create two tasks
            for (int j = 0; j < taskContents[i].length; j++) {
                given().
                    contentType(ContentType.JSON).
                    header("Authorization", "Bearer " + token).
                    body(
                        Map.of(
                            "taskContent", taskContents[i][j]
                        )).
                    when().
                        post("/tasks").
                    then().
                        statusCode(201);
            }

            tokens[i] = token;
        }
    }

    /**
     * TEST 1
     * Sending an http request to GET localhost:8080/tasks/current_user when logged in 
     * as a user and receiving the correct tasks
     * 
     * Expected Response:
     *  Status Code: 200
     */
    @Test
    @DisplayName("Successfully retrived all tasks by currently logged in user")
    void getTasksByCurrentUserSuccessful() {
        // get tasks by john_doe
        Response response = given().
            contentType(ContentType.JSON).
            header("Authorization", "Bearer " + tokens[0]).
            when().
                get("/tasks/current_user").
            then().
                statusCode(200).
                extract().
                response();

        // there should be two tasks, one that says "Hello", and one that says "World"
        assertEquals(2, response.jsonPath().getList("$").size());
        response.then().assertThat().body("[0].taskContent", equalTo("Hello"));
        response.then().assertThat().body("[1].taskContent", equalTo("World"));
    }

    /**
     * TEST 2
     * Sending an http request to GET localhost:8080/tasks/current_user when 
     * not logged in as a user (or at least no token in Authorization header)
     * 
     * Expected Response:
     *  Status Code: 403
     */
    @Test
    @DisplayName("Unsuccessfully retrival of tasks - no user to authenticate (no token sent in header)")
    void getTasksByCurrentUserNoAuthorization() {
        // no token given, no user to check tasks of
        given().
            contentType(ContentType.JSON).
            when().
                get("/tasks/current_user").
            then().
                statusCode(401);
    }

    /**
     * TEST 2
     * Sending an http request to GET localhost:8080/tasks/current_user when logged in 
     * as a user and NOT receiving other users' tasks
     * 
     * Expected Response:
     *  Status Code: 200
     */
    @Test
    @DisplayName("Successfully retrived all tasks by currently logged in user")
    void getTasksByCurrentUser() {
        // get tasks by jane_doe
        Response response = given().
            contentType(ContentType.JSON).
            header("Authorization", "Bearer " + tokens[1]).
            when().
                get("/tasks/current_user").
            then().
                statusCode(200).
                extract().
                response();

        // jane_doe's tasks should not include john_doe's tasks
        assertEquals(2, response.jsonPath().getList("$").size());
        response.then().assertThat().body("[0].taskContent", not("Hello"));
        response.then().assertThat().body("[0].taskContent", equalTo("FOO"));
        response.then().assertThat().body("[1].taskContent", not("World"));
        response.then().assertThat().body("[1].taskContent", equalTo("BAR"));
    }
}