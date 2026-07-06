package com.theblind.todo.e2e.steps;

import com.theblind.todo.e2e.TestDataHelper;
import com.theblind.todo.e2e.config.BrowserConfig;
import com.theblind.todo.e2e.pages.LoginPage;
import com.theblind.todo.e2e.pages.RegisterPage;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

// Step definitions for auth_login.feature and auth_register.feature.
public class AuthSteps {

    // BrowserConfig has no @Component so Spring can't autowire it directly.
    // Cucumber-Spring handles it through constructor injection instead.
    private final BrowserConfig browserConfig;

    @Autowired
    private TestDataHelper testDataHelper;

    private LoginPage loginPage;
    private RegisterPage registerPage;

    public AuthSteps(BrowserConfig browserConfig) {
        this.browserConfig = browserConfig;
    }

    // Frontend base URL — pass -De2e.baseUrl=http://localhost:4200 to override
    private String baseUrl() {
        return System.getProperty("e2e.baseUrl", "http://localhost:4200");
    }

    // Wipe the database and set up fresh page objects before each scenario.
    // Order 100 ensures this runs after BrowserConfig.openBrowser() (order 0 by default)
    // so the WebDriver is ready when we create the page objects.
    @Before(order = 100)
    public void setUp() {
        testDataHelper.cleanDatabase();
        loginPage    = new LoginPage(browserConfig.getDriver());
        registerPage = new RegisterPage(browserConfig.getDriver());
    }

    // ── Given ────────────────────────────────────────────────────────────────

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
        loginPage.navigateTo(baseUrl());
    }

    @Given("I am on the register page")
    public void iAmOnTheRegisterPage() {
        registerPage.navigateTo(baseUrl());
    }

    // Creates a user via the API so login tests have an account to work with
    @Given("a registered user exists with username {string} and password {string}")
    public void aRegisteredUserExistsWith(String username, String password) {
        testDataHelper.registerUser(username, password);
    }

    // Logs in via the UI and stores the fact that we're authenticated
    @Given("I am logged in as {string} with password {string}")
    public void iAmLoggedInAs(String username, String password) {
        loginPage.navigateTo(baseUrl());
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
        loginPage.clickLogin();
        assertThat(loginPage.isOnDashboard())
                .as("Should be on dashboard after logging in")
                .isTrue();
    }

    // ── When ─────────────────────────────────────────────────────────────────

    @When("I enter username {string} and password {string}")
    public void iEnterUsernameAndPassword(String username, String password) {
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
    }

    @When("I click the login button")
    public void iClickTheLoginButton() {
        loginPage.clickLogin();
    }

    @When("I fill in username {string} password {string} and confirm password {string}")
    public void iFillInRegistrationForm(String username, String password, String confirm) {
        registerPage.enterUsername(username);
        registerPage.enterPassword(password);
        registerPage.enterConfirmPassword(confirm);
    }

    @When("I click the create account button")
    public void iClickTheCreateAccountButton() {
        registerPage.clickCreateAccount();
    }

    @When("I navigate to the login page")
    public void iNavigateToTheLoginPage() {
        loginPage.navigateTo(baseUrl());
    }

    // ── Then ─────────────────────────────────────────────────────────────────

    @Then("I should be redirected to the dashboard")
    public void iShouldBeRedirectedToTheDashboard() {
        assertThat(loginPage.isOnDashboard())
                .as("Should be redirected to /dashboard after login")
                .isTrue();
    }

    @Then("I should stay on the login page")
    public void iShouldStayOnTheLoginPage() {
        assertThat(loginPage.isOnLoginPage())
                .as("Should remain on /login when credentials are blank")
                .isTrue();
    }

    @Then("I should see a login error message")
    public void iShouldSeeALoginErrorMessage() {
        assertThat(loginPage.hasErrorMessage())
                .as("An error message should be visible after a failed login")
                .isTrue();
    }

    @Then("I should see a registration success message")
    public void iShouldSeeARegistrationSuccessMessage() {
        assertThat(registerPage.isRegistrationSuccessful())
                .as("Success message should appear after valid registration")
                .isTrue();
    }

    @Then("I should see a username validation error")
    public void iShouldSeeAUsernameValidationError() {
        assertThat(registerPage.hasUsernameError())
                .as("A username validation error should be visible")
                .isTrue();
    }

    @Then("I should see a password validation error")
    public void iShouldSeeAPasswordValidationError() {
        assertThat(registerPage.hasPasswordError())
                .as("A password validation error should be visible")
                .isTrue();
    }

    @Then("I should see a confirm password mismatch error")
    public void iShouldSeeAConfirmPasswordMismatchError() {
        assertThat(registerPage.hasConfirmPasswordError())
                .as("A password mismatch error should be visible")
                .isTrue();
    }

    @Then("I should see a registration error message")
    public void iShouldSeeARegistrationErrorMessage() {
        // Staying on register page after submit means registration failed
        assertThat(registerPage.isOnRegisterPage())
                .as("Should stay on register page when registration fails")
                .isTrue();
    }
}