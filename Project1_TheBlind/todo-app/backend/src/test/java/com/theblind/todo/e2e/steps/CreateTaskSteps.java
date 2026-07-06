package com.theblind.todo.e2e.steps;

import com.theblind.todo.e2e.TestDataHelper;
import com.theblind.todo.e2e.config.BrowserConfig;
import com.theblind.todo.e2e.pages.DashboardPage;
import com.theblind.todo.e2e.pages.LoginPage;

import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CreateTaskSteps {
    // helper functions
    @Autowired
    private TestDataHelper testDataHelper;

    // pages — initialized lazily so the driver is guaranteed to exist when first used
    private LoginPage loginPage;
    private DashboardPage dashboardPage;

    // used for registration, login, and authentication purposes
    private String username = "john_doe";
    private String password = "Abc**4";

    // BrowserConfig has no @Component so Spring can't autowire it directly.
    // Cucumber-Spring handles it through constructor injection instead.
    private final BrowserConfig browserConfig;

    public CreateTaskSteps(BrowserConfig browserConfig) {
        this.browserConfig = browserConfig;
    }

    // Wipe the database before each scenario and reset the page object references.
    // Order 100 ensures this runs after BrowserConfig.openBrowser() (order 0),
    // but we still don't create page objects here — the driver may not be fully
    // ready until the first step method is called.
    @Before(order = 100)
    public void setUp() {
        testDataHelper.cleanDatabase();
        // Nulled out so getLoginPage() / getDashboardPage() rebuild them with
        // the fresh driver that BrowserConfig just created for this scenario.
        loginPage     = null;
        dashboardPage = null;
    }

    /** Returns the LoginPage, creating it the first time it is needed. */
    private LoginPage getLoginPage() {
        if (loginPage == null) {
            loginPage = new LoginPage(browserConfig.getDriver());
        }
        return loginPage;
    }

    /** Returns the DashboardPage, creating it the first time it is needed. */
    private DashboardPage getDashboardPage() {
        if (dashboardPage == null) {
            dashboardPage = new DashboardPage(browserConfig.getDriver());
        }
        return dashboardPage;
    }

    @Given("the user has registered their account")
    public void the_user_has_registered_their_account() {
        testDataHelper.registerUser(this.username, this.password);
    }

    @Given("the user has logged into their account")
    public void the_user_has_logged_into_their_account() {
        getLoginPage().navigateTo("http://localhost:4200");
        getLoginPage().enterUsername(this.username);
        assertThat(getLoginPage().verifyUsernameInput())
                .as("Username should be typed")
                .isTrue();
        getLoginPage().enterPassword(this.password);
        assertThat(getLoginPage().verifyPasswordInput())
                .as("Password should be typed")
                .isTrue();
        getLoginPage().clickLogin();
        assertThat(getLoginPage().hasErrorMessage())
                .as("Should not have error logging in")
                .isFalse();
        assertThat(getLoginPage().isOnDashboard())
                .as("Should be redirected to dashboard after logging in")
                .isTrue();
    }

    @Given("the user is at the dashboard page")
    public void the_user_is_at_the_dashboard_page() {
        assertThat(getDashboardPage().isOnDashboardPage())
                .as("Should be on dashboard")
                .isTrue();
        assertThat(getDashboardPage().ensurePageIsLoaded())
                .as("Dashboard elements should be fully loaded")
                .isTrue();
    }

    // --- Scenario 1: Valid task

    @When("the user inputs text")
    public void the_user_inputs_text() {
        getDashboardPage().enterPrimaryTaskText("Hello, world!");
    }

    @When("the user clicks the add task button")
    public void the_user_clicks_the_add_task_button() {
        getDashboardPage().clickCreateTaskButton();
    }

    @Then("the task should be created and the UI should be updated")
    public void the_task_should_be_created_and_the_ui_should_be_updated() {
        String text = getDashboardPage().getPrimaryTaskText();
        assertThat(text)
                .as("Primary task should have been created with correct content")
                .isEqualTo("Hello, world!");
    }

    // --- Scenario 2: Invalid task, no text
    
    @When("the user does not input any text")
    public void the_user_does_not_input_any_text() {
        getDashboardPage().enterPrimaryTaskText("");
    }

    @When("the user clicks the add task button while input is blank")
    public void the_user_clicks_the_add_task_button_while_input_is_blank() {
        // Use the no-wait variant — blank input means no task is created and
        // li.task-node will never appear, so the normal click would time out.
        getDashboardPage().clickCreateTaskButtonNoWait();
    }

    @Then("the task should not be created and the UI should not change")
    public void the_task_should_not_be_created_and_the_ui_should_not_change() {
        assertThat(getDashboardPage().checkIfNoPrimaryTask())
                .as("No task should have been created")
                .isTrue();
    }

}
