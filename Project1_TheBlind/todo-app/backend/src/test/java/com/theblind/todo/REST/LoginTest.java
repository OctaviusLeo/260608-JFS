package com.theblind.todo.REST;

import com.theblind.todo.Repo.AccountRepo;

// RESTAssured Team reccommends these imports for ease of framework use
import static io.restassured.RestAssured.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

// tells app that, during testing, for this class, the web environment is actually real
// real and during testing, it  is listening on a random port
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// overrides default properties file
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Login RESTAssured unit tests")
public class LoginTest {
    // Port in variable
    @LocalServerPort
    private int port;

    // Injecting the repo directly lets us wipe the database before each test,
    // guaranteeing a clean slate regardless of test order or context reuse.
    @Autowired
    private AccountRepo accountRepo;

    @BeforeEach
    public void setUp() {
        // Wipe all users so the unique-username constraint never blocks registration
        accountRepo.deleteAll();

        RestAssured.baseURI = "http://localhost/api";
        RestAssured.port = port;

        // Register john_doe so login tests have a real account to authenticate against.
        // This must return 201; if it doesn't, the test setup is broken and we want
        // an immediate failure rather than a misleading login error.
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"john_doe\",\"password\":\"AbcDe**123\"}")
            .when()
                .post("/register")
            .then()
                .statusCode(201);
    }

    /**
     * TEST 1
     * Sending an http request to POST localhost:8080/auth/login with correct credentials
     * 
     * Expected Response:
     *  Status Code: 200
     */
    @Test
    @DisplayName("Successful login - credentials match")
    void registerUserSuccess() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"john_doe\",\"password\":\"AbcDe**123\"}")
            .when()
                .post("/auth/login")
            .then()
                .statusCode(200);
    }

    /**
     * TEST 2
     * Sending an http request to POST localhost:8080/auth/login when username isn't in database
     * 
     * Expected Response:
     *  Status Code: 400
     */
    @Test
    @DisplayName("Unsuccessful login: User does not exist")
    void loginUserInvalidUsername1() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"jane_doe\",\"password\":\"AbcDe**123\"}")
            .when()
                .post("/auth/login")
            .then()
                .statusCode(400);
    }

    /**
     * TEST 3
     * Sending an http request to POST localhost:8080/auth/login when username is null
     * 
     * Expected Response:
     *  Status Code: 400
     */
    @Test
    @DisplayName("Unsuccessful login: Null username")
    void loginUserInvalidUsername2() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"password\":\"AbcDe**123\"}")
            .when()
                .post("/auth/login")
            .then()
                .statusCode(400);
    }

    /**
     * TEST 4
     * Sending an http request to POST localhost:8080/auth/login when password doesn't match
     * 
     * Expected Response:
     *  Status Code: 400
     */
    @Test
    @DisplayName("Unsuccessful login: Wrong password")
    void loginUserInvalidPassword1() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"john_doe\",\"password\":\"AbcDe**12\"}")
            .when()
                .post("/auth/login")
            .then()
                .statusCode(400);
    }

    /**
     * TEST 5
     * Sending an http request to POST localhost:8080/auth/login when password is null
     * 
     * Expected Response:
     *  Status Code: 400
     */
    @Test
    @DisplayName("Unsuccessful login: Null password")
    void loginUserInvalidPassword2() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"john_doe\"}")
            .when()
                .post("/auth/login")
            .then()
                .statusCode(400);
    }

    /**
     * TEST 6
     * Sending an http request to POST localhost:8080/auth/login when username isn't in database
     * and the password is incorrect
     * 
     * Expected Response:
     *  Status Code: 400
     */
    @Test
    @DisplayName("Unsuccessful login: User does not exist AND password is incorrect")
    void loginUserInvalidUsernameAndPassword1() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"jane_doe\",\"password\":\"AbcDe**12\"}")
            .when()
                .post("/auth/login")
            .then()
                .statusCode(400);
    }

    /**
     * TEST 7
     * Sending an http request to POST localhost:8080/auth/login when username and password are null
     * 
     * Expected Response:
     *  Status Code: 400
     */
    @Test
    @DisplayName("Unsuccessful login: Null username AND null password")
    void loginUserInvalidUsernameAndPassword2() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
            .when()
                .post("/auth/login")
            .then()
                .statusCode(400);
    }

    /**
     * TEST 8
     * Sending an http request to POST localhost:8080/auth/login when credentials match 
     * and verifying authentication occurs by looking in the response body for a JSON Web Token
     * 
     * Expected Response:
     *  Status Code: 200
     *  Body: Contains non-blank 'token' and 'expiresIn' fields
     */
    @Test
    @DisplayName("Successful authorization - response body contains JWT token and expiration time limit")
    void loginSuccessful_responseBodyContainsTokenAndExpiresIn() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"john_doe\",\"password\":\"AbcDe**123\"}")
            .when()
                .post("/auth/login")
            .then()
                .statusCode(200)
                // token must be present and non-empty
                .body("token", org.hamcrest.Matchers.notNullValue())
                .body("token", org.hamcrest.Matchers.not(org.hamcrest.Matchers.emptyString()))
                // expiresIn must be present and a positive number
                .body("expiresIn", org.hamcrest.Matchers.notNullValue())
                .body("expiresIn", org.hamcrest.Matchers.greaterThan(0));
    }
}
