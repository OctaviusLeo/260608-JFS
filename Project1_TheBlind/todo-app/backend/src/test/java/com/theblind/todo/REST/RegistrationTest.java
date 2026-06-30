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
    @DisplayName("Successful user registration")
    void registerUserSuccess() {
        User testUser1, testUser2;

        // minimum 5 chars
        testUser1 = new User();
        testUser1.setUsername("john_");
        testUser1.setPassword("Ac**4");

        // maximum 15 chars
        testUser2 = new User();
        testUser2.setUsername("john_doe_wkivnd");
        testUser2.setPassword("Abc**4wowmespal");

        given().
            contentType(ContentType.JSON).
            body(testUser1).
            when().
                post("/register").
            then().
                statusCode(201).
                body("username", equalTo("john_"));

        given().
            contentType(ContentType.JSON).
            body(testUser2).
            when().
                post("/register").
            then().
                statusCode(201).
                body("username", equalTo("john_doe_wkivnd"));
    }

    @Test
    @DisplayName("Unsuccessful registration: Invalid username")
    void registerUserInvalidUsername() {
        User testUser1, testUser2, testUser3, testUser4, testUser5;

        // 4 chars (just below minimum)
        testUser1 = new User();
        testUser1.setUsername("john");
        testUser1.setPassword("Abc**4");

        // 16 chars (just above maximum)
        testUser2 = new User();
        testUser2.setUsername("john_doe_wkivndq");
        testUser2.setPassword("Abc**4");

        // spaces included
        testUser3 = new User();
        testUser3.setUsername("john_   ");
        testUser3.setPassword("Abc**4");

        // empty
        testUser4 = new User();
        testUser4.setUsername("");
        testUser4.setPassword("Abc**4");
    
        // null
        testUser5 = new User();
        testUser5.setUsername("");
        testUser5.setPassword("Abc**4");

        User[] users = {
            testUser1,
            testUser2,
            testUser3,
            testUser4,
            testUser5
        };

        for (int i = 0; i < users.length; i++) {
            given().
                contentType(ContentType.JSON).
                body(users[i]).
                when().
                    post("/register").
                then().
                    statusCode(400);
        }
    }
}
