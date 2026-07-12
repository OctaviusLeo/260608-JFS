Feature: Read Task
  Background:
    Given the user has registered their account
    Given the user has logged into their account
    Given the user is at the dashboard page
    Given the user inputs text 
    Given the user clicks the add task button
    Given the task should be created and the UI should be updated

  Scenario: Tasks will persist after logout
    When the user clicks on the logout button
    And the user logs back in
    Then the task should remain on dashboard