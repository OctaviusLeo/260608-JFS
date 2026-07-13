Feature: Delete Task
  Background:
    Given the user has registered their account
    Given the user has logged into their account
    Given the user is at the dashboard page
    Given the user inputs text 
    Given the user clicks the add task button

  Scenario: Successfully delete a task
    When the user clicks on the delete button of a task to update it
    Then the task should be removed from the dashboard