@child-tasks
Feature: Child Task Management

  As a logged-in user,
  I want to manage subtasks under my existing tasks,
  so that I can organize work into smaller, trackable steps.

  Background:
    Given a registered user exists with username "taskUser" and password "Test**12"
    And I am authenticated as "taskUser" with password "Test**12"
    And I am on the dashboard page
    And I have created a task "Buy groceries"

  # ── Creating child tasks ─────────────────────────────────────────────────

  Scenario: Add a child task to an existing task
    When I create a child task "Buy milk" under "Buy groceries"
    Then I should see "Buy milk" as a subtask of "Buy groceries"

  Scenario: Add multiple child tasks to the same parent
    When I create a child task "Buy milk" under "Buy groceries"
    And I create a child task "Buy eggs" under "Buy groceries"
    And I create a child task "Buy bread" under "Buy groceries"
    Then "Buy groceries" should have 3 subtasks

  Scenario: Cannot create a child task with empty content
    When I attempt to create a child task "" under "Buy groceries"
    Then the child task should not be created
    And I should see a validation error for task content

  Scenario: Cannot create a child task exceeding 50 characters
    When I attempt to create a child task "This content is intentionally longer than the allowed fifty character limit" under "Buy groceries"
    Then I should see "This content is intentionally longer than the allo" as a subtask of "Buy groceries"

  # ── Completing child tasks ───────────────────────────────────────────────

  Scenario: Mark a child task as complete
    Given "Buy groceries" has a child task "Buy milk"
    When I mark the subtask "Buy milk" as complete
    Then the subtask "Buy milk" should show as complete

  Scenario: Unmark a completed child task
    Given "Buy groceries" has a completed child task "Buy milk"
    When I mark the subtask "Buy milk" as incomplete
    Then the subtask "Buy milk" should show as incomplete

  # ── Editing child tasks ──────────────────────────────────────────────────

  Scenario: Edit a child task's content
    Given "Buy groceries" has a child task "Buy milk"
    When I update the subtask "Buy milk" content to "Buy oat milk"
    Then I should see "Buy oat milk" as a subtask of "Buy groceries"
    And "Buy milk" should no longer appear as a subtask

  Scenario: Cannot edit a child task to have empty content
    Given "Buy groceries" has a child task "Buy milk"
    When I attempt to update the subtask "Buy milk" content to ""
    Then I should see a validation error for task content

  Scenario: Cannot edit a child task to exceed 50 characters
    Given "Buy groceries" has a child task "Buy milk"
    When I attempt to update the subtask "Buy milk" content to "This content is intentionally longer than the allowed fifty character limit"
    Then I should see "This content is intentionally longer than the allo" as a subtask of "Buy groceries"

  # ── Deleting child tasks ─────────────────────────────────────────────────

  Scenario: Delete a single child task
    Given "Buy groceries" has a child task "Buy milk"
    And "Buy groceries" has a child task "Buy eggs"
    When I delete the subtask "Buy milk"
    Then "Buy milk" should no longer appear as a subtask
    And I should see "Buy eggs" as a subtask of "Buy groceries"

  Scenario: Deleting a parent task removes all its child tasks
    Given "Buy groceries" has a child task "Buy milk"
    And "Buy groceries" has a child task "Buy eggs"
    When I delete the task "Buy groceries"
    Then the task "Buy groceries" should no longer exist
    And the task "Buy milk" should no longer exist
    And the task "Buy eggs" should no longer exist

  # ── Nested subtasks ──────────────────────────────────────────────────────

  Scenario: Create a subtask under another subtask
    Given "Buy groceries" has a child task "Buy dairy"
    When I create a child task "Buy whole milk" under "Buy dairy"
    Then I should see "Buy whole milk" as a subtask of "Buy dairy"

  Scenario: Deleting a subtask cascades to its children
    Given "Buy groceries" has a child task "Buy dairy"
    And "Buy dairy" has a child task "Buy whole milk"
    When I delete the subtask "Buy dairy"
    Then "Buy dairy" should no longer appear as a subtask
    And the task "Buy whole milk" should no longer exist
