package com.theblind.todo.REST;

import com.theblind.todo.Entity.User;

// RESTAssured Team reccommends these imports for ease of framework use
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
@DisplayName("Registration RESTAssured unit tests")
public class RegistrationTest {
    // Port in variable
    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "http://localhost/api";
        RestAssured.port = port;
    }

    @Test
    @DisplayName("Successful user registration - minimum chars")
    void registerUserSuccess1() {
        User testUser = new User();
        testUser.setUsername("john_");
        testUser.setPassword("Ac**4");

        given().
            contentType(ContentType.JSON).
            body(testUser).
            when().
                post("/register").
            then().
                statusCode(201).
                body("username", equalTo("john_"));
    }

    @Test
    @DisplayName("Successful user registration - maximum chars")
    void registerUserSuccess2() {
        User testUser = new User();
        testUser.setUsername("john_doe_wkivnd");
        testUser.setPassword("Abc**4wowmespal");

        given().
            contentType(ContentType.JSON).
            body(testUser).
            when().
                post("/register").
            then().
                statusCode(201).
                body("username", equalTo("john_doe_wkivnd"));
    }

    @Test
    @DisplayName("Unsuccessful registration: Username below minimum chars")
    void registerUserInvalidUsername1() {
        User testUser = new User();
        testUser.setUsername("john");
        testUser.setPassword("Abc**4");

        given().
            contentType(ContentType.JSON).
            body(testUser).
            when().
                post("/register").
            then().
                statusCode(400);
    }

    @Test
    @DisplayName("Unsuccessful registration: Username above maximum chars")
    void registerUserInvalidUsername2() {
        User testUser = new User();
        testUser.setUsername("john_doeqpskjdir");
        testUser.setPassword("Abc**4");

        given().
            contentType(ContentType.JSON).
            body(testUser).
            when().
                post("/register").
            then().
                statusCode(400);
    }

    @Test
    @DisplayName("Unsuccessful registration: Username contains spaces")
    void registerUserInvalidUsername3() {
        User testUser = new User();
        testUser.setUsername("john doe");
        testUser.setPassword("Abc**4");

        given().
            contentType(ContentType.JSON).
            body(testUser).
            when().
                post("/register").
            then().
                statusCode(400);
    }

    @Test
    @DisplayName("Unsuccessful registration: Username is null")
    void registerUserInvalidUsername4() {
        User testUser = new User();
        testUser.setUsername(null);
        testUser.setPassword("Abc**4");

        given().
            contentType(ContentType.JSON).
            body(testUser).
            when().
                post("/register").
            then().
                statusCode(400);
    }

    @Test
    @DisplayName("Unsuccessful registration: Username is empty")
    void registerUserInvalidUsername5() {
        User testUser = new User();
        testUser.setUsername("     ");
        testUser.setPassword("Abc**4");

        given().
            contentType(ContentType.JSON).
            body(testUser).
            when().
                post("/register").
            then().
                statusCode(400);
    }
}
