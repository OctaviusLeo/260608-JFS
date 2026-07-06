package com.theblind.todo.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

// Page object for the login page (/login).
// Keeps all selectors in one place so step definitions don't deal with HTML.
public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // IDs from login-form.component.html
    private final By usernameInput = By.id("username");
    private final By passwordInput = By.id("password");
    private final By loginButton   = By.cssSelector("button.btn-submit");
    // Selector for the API-level login failure message only — NOT the field validation messages
    private final By errorMessage  = By.cssSelector(".error-msg.login-error small");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        // 10 seconds covers the round-trip to the backend + Angular navigation
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Opens the login page and waits for the username field to appear
    public void navigateTo(String baseUrl) {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput));
    }

    public void enterUsername(String username) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput));
        field.clear();
        field.sendKeys(username);
    }

    public void enterPassword(String password) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        field.clear();
        field.sendKeys(password);
    }

    public void clickLogin() {
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
        // Block until the async HTTP response has been processed:
        //   (a) URL leaves /login  → successful login, Angular navigated to /dashboard
        //   (b) API error element appears → bad credentials, still on /login
        // Field-level validation messages (.error-msg without .login-error) are
        // deliberately excluded — they can appear synchronously before the click.
        wait.until(ExpectedConditions.or(
            ExpectedConditions.not(ExpectedConditions.urlContains("/login")),
            ExpectedConditions.visibilityOfElementLocated(errorMessage)
        ));
    }

    // Returns the API error message text, or empty string if none is showing
    public String getErrorMessage() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText();
        } catch (Exception e) {
            return "";
        }
    }

    // True when the API login-error message is visible
    public boolean hasErrorMessage() {
        return !getErrorMessage().isEmpty();
    }

    // True when the username input has a non-empty value
    public boolean verifyUsernameInput() {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput));
        String value = field.getAttribute("value");
        return value != null && !value.isEmpty();
    }

    // True when the password input has a non-empty value
    public boolean verifyPasswordInput() {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        String value = field.getAttribute("value");
        return value != null && !value.isEmpty();
    }

    // True if the browser is still on /login
    public boolean isOnLoginPage() {
        return driver.getCurrentUrl().contains("/login");
    }

    // True if the browser has reached /dashboard
    public boolean isOnDashboard() {
        try {
            wait.until(ExpectedConditions.urlContains("/dashboard"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
