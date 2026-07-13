Feature: Update Task
  Background:
    Given the user has registered their account
    Given the user has logged into their account
    Given the user is at the dashboard page
    Given the user inputs text 
    Given the user clicks the add task button

  Scenario: Successfully updates a task
    When the user clicks on the edit button of a task to update it
    And the user inputs new text
    Then the task should be updated and the UI should be updated

  Scenario: Successfully updates a task
    When the user clicks on the edit button of a task to update it
    And the user inputs no text
    Then the task should not be updated