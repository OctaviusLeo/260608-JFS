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

public class ReadTaskSteps {
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

    public ReadTaskSteps(BrowserConfig browserConfig) {
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

    @When("the user clicks on the logout button")
    public void the_user_clicks_on_the_logout_button() {
        getDashboardPage().logout();
        assertThat(getLoginPage().isOnLoginPage())
                .as("Should be redirected to login after logging out")
                .isTrue();
    }
    @When("the user logs back in")
    public void the_user_logs_back_in() {
        getLoginPage().enterUsername(this.username);
        assertThat(getLoginPage().verifyUsernameInput())
                .as("Username should be typed (again)")
                .isTrue();
        getLoginPage().enterPassword(this.password);
        assertThat(getLoginPage().verifyPasswordInput())
                .as("Password should be typed (again)")
                .isTrue();
        getLoginPage().clickLogin();
        assertThat(getLoginPage().hasErrorMessage())
                .as("Should not have error logging in (again)")
                .isFalse();
        assertThat(getDashboardPage().isOnDashboardPage())
                .as("Should be redirected to dashboard after logging in (again)")
                .isTrue();
    }
    @Then("the task should remain on dashboard")
    public void the_task_should_remain_on_dashboard() {
        String text = getDashboardPage().getPrimaryTaskText();
        assertThat(text)
                .as("Primary task should have been created with correct content")
                .isEqualTo("Hello, world!");
    }


}
