Feature: Create Task
  Background: User has registered and has logged in
    Given the user has registered their account
    Given the user has logged into their account
    Given the user is at the dashboard page

  Scenario: Creating a task
    When the user inputs text 
    And the user clicks the add task button
    Then the task should be created and the UI should be updated
