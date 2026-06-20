package com.theblind.todo;

import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

@DisplayName("User registration unit tests")
public class UserRegistrationTests {
	ApplicationContext app;
    HttpClient webClient;

    private final String REGISTRATION_API = "http://localhost:8080/api/register";

    private final String JSON_JOHN_DOE_CORRECT = "{\"username\":\"john_doe\",\"password\":\"AbcDe**123\"}";
    private final String JSON_JOHN_DOE_INVALID_NAME = "{\"username\":\"john\",\"password\":\"AbcDe**123\"}";
    private final String JSON_JOHN_DOE_INVALID_PASSWORD = "{\"username\":\"john_doe\",\"password\":\"Abc\"}";
    private final String JSON_JOHN_DOE_INVALID_NAME_AND_PASSWORD = "{\"username\":\"john\",\"password\":\"abc\"}";
    private final String JSON_JOHN_DOE_NULL_NAME = "{\"password\":\"AbcDe**123\"}";
    private final String JSON_JOHN_DOE_NULL_PASSWORD = "{\"username\":\"john_doe\"}";
    private final String JSON_JOHN_DOE_NULL_NAME_AND_PASSWORD = "{}";

    /**
     * Before every test, reset the database, restart the Javalin app, and create a new webClient and ObjectMapper
     * for interacting locally on the web.
     */
    @BeforeEach
    public void setUp() throws InterruptedException {
        webClient = HttpClient.newHttpClient();
        String[] args = new String[] {};
        app = SpringApplication.run(TodoApplication.class, args);
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
     * Sending an http request to POST localhost:8080/auth/register when username does not exist in the system
     * 
     * Expected Response:
     *  Status Code: 201
     */
    @Test
    public void registerUserSuccessful() throws IOException, InterruptedException {
    	HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(REGISTRATION_API))
                .POST(HttpRequest.BodyPublishers.ofString(JSON_JOHN_DOE_CORRECT))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        Assertions.assertEquals(201, status, "Expected Status Code 201 - Actual Code was: " + status);
    }

     /**
     * TEST 2
     * Sending an http request to POST localhost:8080/auth/register when username already exists in system
     * 
     * Expected Response:
     *  Status Code: 400
     */
    @Test
    public void registerUserDuplicateUsername() throws IOException, InterruptedException {
    	HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(REGISTRATION_API))
                .POST(HttpRequest.BodyPublishers.ofString(JSON_JOHN_DOE_CORRECT))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response1 = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response2 = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        int status1 = response1.statusCode();
        int status2 = response2.statusCode();
        Assertions.assertEquals(201, status1, "Expected Status Code 201 - Actual Code was: " + status1);
        Assertions.assertEquals(400, status2, "Expected Status Code 400 - Actual Code was: " + status2);
    }

    /**
     * TEST 3
     * Sending an http request to POST localhost:8080/auth/register when username is invalid
     * 
     * Expected Response:
     *  Status Code: 400
     */
    @Test
    public void registerUserInvalidUsername() throws IOException, InterruptedException {
    	HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(REGISTRATION_API))
                .POST(HttpRequest.BodyPublishers.ofString(JSON_JOHN_DOE_INVALID_NAME))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }
    
    /**
     * TEST 4
     * Sending an http request to POST localhost:8080/auth/register when password is invalid
     * 
     * Expected Response:
     *  Status Code: 400
     */
    @Test
    public void registerUserInvalidPassword() throws IOException, InterruptedException {
    	HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(REGISTRATION_API))
                .POST(HttpRequest.BodyPublishers.ofString(JSON_JOHN_DOE_INVALID_PASSWORD))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }

    /**
     * TEST 5
     * Sending an http request to POST localhost:8080/auth/register when username AND password is invalid
     * 
     * Expected Response:
     *  Status Code: 400
     */
    @Test
    public void registerUserInvalidUsernameAndPassword() throws IOException, InterruptedException {
    	HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(REGISTRATION_API))
                .POST(HttpRequest.BodyPublishers.ofString(JSON_JOHN_DOE_INVALID_NAME_AND_PASSWORD))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }

    /**
     * TEST 6
     * Sending an http request to POST localhost:8080/auth/register when username is null
     * 
     * Expected Response:
     *  Status Code: 400
     */
    @Test
    public void registerUserNullUsername() throws IOException, InterruptedException {
    	HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(REGISTRATION_API))
                .POST(HttpRequest.BodyPublishers.ofString(JSON_JOHN_DOE_NULL_NAME))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }

    /**
     * TEST 7
     * Sending an http request to POST localhost:8080/auth/register when password is null
     * 
     * Expected Response:
     *  Status Code: 400
     */
    @Test
    public void registerUserNullPassword() throws IOException, InterruptedException {
    	HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(REGISTRATION_API))
                .POST(HttpRequest.BodyPublishers.ofString(JSON_JOHN_DOE_NULL_PASSWORD))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }

    /**
     * TEST 8
     * Sending an http request to POST localhost:8080/auth/register when username AND password is null
     * 
     * Expected Response:
     *  Status Code: 400
     */
    @Test
    public void registerUserNullUsernameAndPassword() throws IOException, InterruptedException {
    	HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(REGISTRATION_API))
                .POST(HttpRequest.BodyPublishers.ofString(JSON_JOHN_DOE_NULL_NAME_AND_PASSWORD))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        int status = response.statusCode();
        Assertions.assertEquals(400, status, "Expected Status Code 400 - Actual Code was: " + status);
    }
}
