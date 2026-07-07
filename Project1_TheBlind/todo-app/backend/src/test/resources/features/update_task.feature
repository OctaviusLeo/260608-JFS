Feature: Update Task
  Scenario: Successfully updates a task
    Given the user has registered
    Given the user has logged in
    Given the user is at the dashboard
    Given the user creates a task
    When the user clicks on the edit button of a task to update it
    And the user inputs new text
    Then the task should be updated and the UI should be updated