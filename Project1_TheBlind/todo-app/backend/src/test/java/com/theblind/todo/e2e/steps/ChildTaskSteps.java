package com.theblind.todo.e2e.steps;

import com.theblind.todo.e2e.config.BrowserConfig;
import com.theblind.todo.e2e.pages.LoginPage;
import com.theblind.todo.e2e.pages.TaskPage;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for child_tasks.feature.
 * Given steps create data through the UI (same backend Angular uses).
 * When/Then steps use TaskPage (Selenium — simulates real user interaction).
 */
public class ChildTaskSteps {

    private final BrowserConfig browserConfig;

    private TaskPage taskPage;
    private LoginPage loginPage;

    public ChildTaskSteps(BrowserConfig browserConfig) {
        this.browserConfig = browserConfig;
    }

    private String baseUrl() {
        return System.getProperty("e2e.baseUrl", "http://localhost:4200");
    }

    @Before(order = 110)
    public void setUp() {
        taskPage = new TaskPage(browserConfig.getDriver());
        loginPage = new LoginPage(browserConfig.getDriver());
    }

    // ── Background steps ──────────────────────────────────────────────────────

    @Given("I am authenticated as {string} with password {string}")
    public void iAmAuthenticatedAs(String username, String password) {
        // Login through the frontend UI
        loginPage.navigateTo(baseUrl());
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
        loginPage.clickLogin();
    }

    @Given("I have created a task {string}")
    public void iHaveCreatedATask(String content) {
        // Create task through the UI — ensures it goes through the same backend
        // that the Angular frontend is connected to.
        taskPage.createTask(content);
    }

    // ── Given: data setup via API ─────────────────────────────────────────────

    @Given("{string} has a child task {string}")
    public void parentHasAChildTask(String parentContent, String childContent) {
        // Create child task through UI — ensures same backend as Angular
        taskPage.addChildTask(parentContent, childContent, true);
    }

    @Given("{string} has a completed child task {string}")
    public void parentHasACompletedChildTask(String parentContent, String childContent) {
        // Create child task through UI, then mark it complete through UI
        taskPage.addChildTask(parentContent, childContent, true);
        taskPage.toggleChildTaskComplete(childContent);
    }

    // ── When: creating child tasks (UI) ───────────────────────────────────────

    @When("I create a child task {string} under {string}")
    public void iCreateAChildTaskUnder(String childContent, String parentContent) {
        taskPage.addChildTask(parentContent, childContent, true);
    }

    @When("I attempt to create a child task {string} under {string}")
    public void iAttemptToCreateAChildTaskUnder(String childContent, String parentContent) {
        taskPage.addChildTask(parentContent, childContent, false);
    }

    // ── When: marking complete/incomplete (UI) ────────────────────────────────

    @When("I mark the subtask {string} as (in)complete")
    public void iMarkTheSubtaskAs(String childContent) {
        taskPage.toggleChildTaskComplete(childContent);
    }

    // ── When: editing child tasks (UI) ────────────────────────────────────────

    @When("I update the subtask {string} content to {string}")
    public void iUpdateTheSubtaskContentTo(String oldContent, String newContent) {
        taskPage.editChildTask(oldContent, newContent, true);
    }
    @When("I attempt to update the subtask {string} content to {string}")
    public void iAttemptToUpdateTheSubtaskContentTo(String oldContent, String newContent) {
        taskPage.editChildTask(oldContent, newContent, false);
    }

    // ── When: deleting (UI) ───────────────────────────────────────────────────

    @When("I delete the (sub)task {string}")
    public void iDeleteTheSubtaskOrTask(String content) {
        taskPage.deleteTask(content);
    }

    // ── Then: positive assertions (UI) ────────────────────────────────────────

    @Then("I should see {string} as a subtask of {string}")
    public void iShouldSeeAsASubtaskOf(String childContent, String parentContent) {
        assertThat(taskPage.isChildTaskVisibleUnderParent(parentContent, childContent))
                .as("'%s' should be visible under '%s'", childContent, parentContent)
                .isTrue();
    }

    @Then("{string} should have {int} subtasks")
    public void parentShouldHaveNSubtasks(String parentContent, int expectedCount) {
        assertThat(taskPage.getChildTaskCount(parentContent))
                .as("'%s' should have %d subtasks", parentContent, expectedCount)
                .isEqualTo(expectedCount);
    }

    @Then("the subtask {string} should show as complete")
    public void theSubtaskShouldShowAsComplete(String content) {
        assertThat(taskPage.isChildTaskComplete(content))
                .as("Subtask '%s' should be complete", content)
                .isTrue();
    }

    @Then("the subtask {string} should show as incomplete")
    public void theSubtaskShouldShowAsIncomplete(String content) {
        assertThat(taskPage.isChildTaskComplete(content))
                .as("Subtask '%s' should be incomplete", content)
                .isFalse();
    }

    // ── Then: negative assertions (UI) ────────────────────────────────────────

    @Then("the child task should not be created")
    public void theChildTaskShouldNotBeCreated() {
        // Validation error visible means creation was rejected
        assertThat(taskPage.hasValidationError())
                .as("Validation error should be shown, child task not created")
                .isTrue();
    }

    @Then("I should see a validation error for task content")
    public void iShouldSeeAValidationErrorForTaskContent() {
        assertThat(taskPage.hasValidationError())
                .as("Validation error for task content should be visible")
                .isTrue();
    }

    @Then("{string} should no longer appear as a subtask")
    public void shouldNoLongerAppearAsASubtask(String content) {
        assertThat(taskPage.isChildTaskVisible(content))
                .as("'%s' should not be visible", content)
                .isFalse();
    }

    @Then("the task {string} should no longer exist")
    public void theTaskShouldNoLongerExist(String content) {
        assertThat(taskPage.taskExists(content))
                .as("Task '%s' should not exist on page", content)
                .isFalse();
    }

    @Then("{string} should still exist as a subtask")
    public void shouldStillExistAsASubtask(String content) {
        assertThat(taskPage.isChildTaskVisible(content))
                .as("'%s' should still be visible", content)
                .isTrue();
    }

    @Given("I am on the dashboard page")
    public void I_am_on_the_dashboard_page() {
        assertThat(loginPage.isOnDashboard())
                .as("Should be on dashboard after logging in")
                .isTrue();
    }
}
