package com.theblind.todo.e2e.steps;

import com.theblind.todo.e2e.TestDataHelper;
import com.theblind.todo.e2e.config.BrowserConfig;
import com.theblind.todo.e2e.pages.DashboardPage;

import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class DeleteTaskSteps {
    // helper functions
    @Autowired
    private TestDataHelper testDataHelper;

    // pages — initialized lazily so the driver is guaranteed to exist when first used
    private DashboardPage dashboardPage;

    // BrowserConfig has no @Component so Spring can't autowire it directly.
    // Cucumber-Spring handles it through constructor injection instead.
    private final BrowserConfig browserConfig;

    public DeleteTaskSteps(BrowserConfig browserConfig) {
        this.browserConfig = browserConfig;
    }

    // Wipe the database before each scenario and reset the page object references.
    // Order 100 ensures this runs after BrowserConfig.openBrowser() (order 0),
    // but we still don't create page objects here — the driver may not be fully
    // ready until the first step method is called.
    @Before(order = 100)
    public void setUp() {
        testDataHelper.cleanDatabase();
        // Nulled out so getDashboardPage() rebuild them with
        // the fresh driver that BrowserConfig just created for this scenario.
        dashboardPage = null;
    }

    /** Returns the DashboardPage, creating it the first time it is needed. */
    private DashboardPage getDashboardPage() {
        if (dashboardPage == null) {
            dashboardPage = new DashboardPage(browserConfig.getDriver());
        }
        return dashboardPage;
    }

    @When("the user clicks on the delete button of a task to update it")
    public void the_user_clicks_on_the_delete_button_of_a_task_to_update_it() {
        getDashboardPage().clickDeleteTaskButton();
    }

    @Then("the task should be removed from the dashboard")
    public void the_task_should_be_removed_from_the_dashboard() {
        assertThat(getDashboardPage().checkIfNoPrimaryTask())
                .as("Primary task should have been deleted")
                .isTrue();
    }
}
