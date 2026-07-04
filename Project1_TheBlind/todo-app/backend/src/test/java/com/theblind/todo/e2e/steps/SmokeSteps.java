package com.theblind.todo.e2e.steps;

import com.theblind.todo.e2e.TestDataHelper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

// Smoke tests — just checks that the app started and the auth endpoints respond.
// No browser needed here, just simple HTTP calls.
public class SmokeSteps {

    @Autowired
    private TestDataHelper testDataHelper;

    private Response lastResponse;

    // Clean up before each scenario so leftover data from a previous test doesn't interfere
    @Before
    public void cleanUp() {
        testDataHelper.cleanDatabase();
    }

    @Given("the backend API is running")
    public void theBackendApiIsRunning() {
        testDataHelper.configureRestAssured();
        assertThat(testDataHelper.getPort())
                .as("Server port should be a positive number")
                .isPositive();
    }

    @When("I send a GET request to the public register endpoint")
    public void iSendAGetRequestToThePublicRegisterEndpoint() {
        lastResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"smokeE2E01\",\"password\":\"Smoke**1\"}")
                .when()
                .post("/register");
    }

    @When("I send a POST request to the login endpoint with empty credentials")
    public void iSendAPostRequestToTheLoginEndpointWithEmptyCredentials() {
        lastResponse = given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post("/auth/login");
    }

    @Then("the response status code should not be 404")
    public void theResponseStatusCodeShouldNotBe404() {
        assertThat(lastResponse.getStatusCode())
                .as("Register endpoint should exist (not 404)")
                .isNotEqualTo(404);
    }

    @Then("the response status should indicate the endpoint exists and is reachable")
    public void theResponseStatusShouldIndicateTheEndpointExistsAndIsReachable() {
        int status = lastResponse.getStatusCode();
        assertThat(status).as("Login endpoint should exist (not 404)").isNotEqualTo(404);
        assertThat(status).as("Login endpoint should not crash (not 5xx)").isLessThan(500);
    }
}
