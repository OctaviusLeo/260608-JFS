package com.theblind.todo;

import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.json.JSONObject;
import org.json.JSONException;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

@DisplayName("User login unit tests")
public class UserLoginTests {
	ApplicationContext app;
    HttpClient webClient;
    private final String REGISTRATION_API = "http://localhost:8080/api/register";

    private final String LOGIN_API = "http://localhost:8080/api/auth/login";

    private final String JSON_JOHN_DOE_CORRECT = "{\"username\":\"john_doe\",\"password\":\"AbcDe**123\"}";
    private final String JSON_JOHN_DOE_INCORRECT_NAME = "{\"username\":\"jane_doe\",\"password\":\"AbcDe**123\"}";
    private final String JSON_JOHN_DOE_INCORRECT_PASSWORD = "{\"username\":\"john_doe\",\"password\":\"321**eDcbA\"}";
    private final String JSON_JOHN_DOE_INCORRECT_NAME_AND_PASSWORD = "{\"username\":\"jane_doe\",\"password\":\"321**eDcbA\"}";
    private final String JSON_JOHN_DOE_NULL_NAME = "{\"password\":\"AbcDe**123\"}";
    private final String JSON_JOHN_DOE_NULL_PASSWORD = "{\"username\":\"john_doe\"}";
    private final String JSON_JOHN_DOE_NULL_NAME_AND_PASSWORD = "{}";


    /**
     * Before every test, reset the database, restart the Javalin app, and create a new webClient and ObjectMapper
     * for interacting locally on the web.
     */
    @BeforeEach
    public void setUp() throws IOException, InterruptedException {
        // set up application
        webClient = HttpClient.newHttpClient();
        String[] args = new String[] {};
        app = SpringApplication.run(TodoApplication.class, args);

        // populate database with John Doe
    	HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(REGISTRATION_API))
                .POST(HttpRequest.BodyPublishers.ofString(JSON_JOHN_DOE_CORRECT))
                .header("Content-Type", "application/json")
                .build();
        webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

        // sleep for half a second
        Thread.sleep(500);
    }

    /**
     * After every test, exit the application.
     */
    @AfterEach
    public void tearDown() throws InterruptedException {
    	Thread.sleep(500);
    	SpringApplication.exit(app);
    }

    /**
     * TEST 1
     * Sending an http request to POST localhost:8080/auth/login when username exists in system
     * 
     * Expected Response:
     *  Status Code: 200
     */
    @Test
    public void loginSuccessful() throws IOException, InterruptedException {
    	HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(LOGIN_API))
                .POST(HttpRequest.BodyPublishers.ofString(JSON_JOHN_DOE_CORRECT))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        Assertions.assertEquals(200, status, "Expected Status Code 200 - Actual Code was: " + status);
    }

    /**
     * TEST 2
     * Sending an http request to POST localhost:8080/auth/login when username doesn't exist in system
     * 
     * Expected Response:
     *  Status Code: 400
     */
    @Test
    public void loginUserDoesNotExist() throws IOException, InterruptedException {
    	HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(LOGIN_API))
                .POST(HttpRequest.BodyPublishers.ofString(JSON_JOHN_DOE_INCORRECT_NAME))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }

    /**
     * TEST 3
     * Sending an http request to POST localhost:8080/auth/login when the passwords don't match
     * 
     * Expected Response:
     *  Status Code: 400
     */
    @Test
    public void loginPasswordIncorrect() throws IOException, InterruptedException {
    	HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(LOGIN_API))
                .POST(HttpRequest.BodyPublishers.ofString(JSON_JOHN_DOE_INCORRECT_PASSWORD))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }

    /**
     * TEST 4
     * Sending an http request to POST localhost:8080/auth/login when both credentials don't match
     * 
     * Expected Response:
     *  Status Code: 400
     */
    @Test
    public void loginNameAndPasswordIncorrect() throws IOException, InterruptedException {
    	HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(LOGIN_API))
                .POST(HttpRequest.BodyPublishers.ofString(JSON_JOHN_DOE_INCORRECT_NAME_AND_PASSWORD))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }

    /**
     * TEST 5
     * Sending an http request to POST localhost:8080/auth/login when username is null
     * 
     * Expected Response:
     *  Status Code: 400
     */
    @Test
    public void loginNullName() throws IOException, InterruptedException {
    	HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(LOGIN_API))
                .POST(HttpRequest.BodyPublishers.ofString(JSON_JOHN_DOE_NULL_NAME))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }

    /**
     * TEST 6
     * Sending an http request to POST localhost:8080/auth/login when password is null
     * 
     * Expected Response:
     *  Status Code: 400
     */
    @Test
    public void loginNullPassword() throws IOException, InterruptedException {
    	HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(LOGIN_API))
                .POST(HttpRequest.BodyPublishers.ofString(JSON_JOHN_DOE_NULL_PASSWORD))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }

    /**
     * TEST 7
     * Sending an http request to POST localhost:8080/auth/login when username AND password are null
     * 
     * Expected Response:
     *  Status Code: 400
     */
    @Test
    public void loginNullNameAndPassword() throws IOException, InterruptedException {
    	HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(LOGIN_API))
                .POST(HttpRequest.BodyPublishers.ofString(JSON_JOHN_DOE_NULL_NAME_AND_PASSWORD))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }

    /**
     * TEST 8
     * Checking if body contains token information to confirm successful authentication
     * after a successful login attempt.
     * 
     * Expected Response:
     *  Status Code: 200
     *  Body: Contains non-blank 'token' and 'expiresIn' fields
     */
    @Test
    public void authenticationSuccessful() throws IOException, InterruptedException, JSONException {
    	HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(LOGIN_API))
                .POST(HttpRequest.BodyPublishers.ofString(JSON_JOHN_DOE_CORRECT))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        JSONObject responseJSON = new JSONObject(response.body());
        Assertions.assertEquals(200, status, "Expected Status Code 200 - Actual Code was: " + status);
        assertFalse(responseJSON.optString("token").isEmpty(), "Expected JSON Web Token to be returned in response body");
        assertFalse(responseJSON.optString("expiresIn").isEmpty(), "Expected JSON Web Token expiration time to be returned in response body");
    }
}