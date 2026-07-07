Feature: Smoke Test

  Scenario: Register endpoint is reachable
    Given the backend API is running
    When I send a GET request to the public register endpoint
    Then the response status code should not be 404

  Scenario: Login endpoint is reachable
    Given the backend API is running
    When I send a POST request to the login endpoint with empty credentials
    Then the response status should indicate the endpoint exists and is reachable
