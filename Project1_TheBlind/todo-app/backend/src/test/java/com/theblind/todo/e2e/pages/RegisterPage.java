package com.theblind.todo.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

// Page object for the register page (/register).
// Keeps all selectors in one place so step definitions don't deal with HTML.
public class RegisterPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // IDs and CSS selectors from registration-form.component.html
    private final By usernameInput        = By.id("username");
    private final By passwordInput        = By.id("password");
    private final By confirmPasswordInput = By.id("confirmPassword");
    private final By submitButton         = By.cssSelector("button.btn-submit");
    private final By successMessage       = By.cssSelector(".success-msg small");
    private final By usernameError        = By.cssSelector(".form-group:nth-child(1) .error-msg small");
    private final By passwordError        = By.cssSelector(".form-group:nth-child(2) .error-msg small");
    private final By confirmPasswordError = By.cssSelector(".form-group:nth-child(3) .error-msg small");

    public RegisterPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    // Opens the register page and waits for the username field to appear
    public void navigateTo(String baseUrl) {
        driver.get(baseUrl + "/register");
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

    public void enterConfirmPassword(String password) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(confirmPasswordInput));
        field.clear();
        field.sendKeys(password);
    }

    public void clickCreateAccount() {
        driver.findElement(submitButton).click();
    }

    // True if the "Account created successfully" banner appeared
    public boolean isRegistrationSuccessful() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // True if a username error is showing
    public boolean hasUsernameError() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(usernameError));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // True if a password error is showing
    public boolean hasPasswordError() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(passwordError));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // True if the "passwords do not match" error is showing
    public boolean hasConfirmPasswordError() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(confirmPasswordError));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Returns the username error text, or empty string if none is showing
    public String getUsernameErrorText() {
        try {
            return driver.findElement(usernameError).getText();
        } catch (Exception e) {
            return "";
        }
    }

    // True if any form error is visible
    public boolean hasAnyError() {
        return hasUsernameError() || hasPasswordError() || hasConfirmPasswordError();
    }

    // True if the browser is still on /register
    public boolean isOnRegisterPage() {
        return driver.getCurrentUrl().contains("/register");
    }
}