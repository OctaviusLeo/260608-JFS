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
    private final By usernameInput    = By.id("username");
    private final By passwordInput    = By.id("password");
    private final By loginButton      = By.cssSelector("button.btn-submit");
    private final By errorMessage     = By.cssSelector(".error-msg.login-error small");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(5));
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
        driver.findElement(loginButton).click();
    }

    // Returns the error message text, or empty string if none is showing
    public String getErrorMessage() {
        try {
            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
            return el.getText();
        } catch (Exception e) {
            return "";
        }
    }

    // True if a login error is showing on the page
    public boolean hasErrorMessage() {
        return !getErrorMessage().isEmpty();
    }

    // True if the browser is still on /login
    public boolean isOnLoginPage() {
        return driver.getCurrentUrl().contains("/login");
    }

    // True if the browser landed on /dashboard after login
    public boolean isOnDashboard() {
        try {
            wait.until(ExpectedConditions.urlContains("/dashboard"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
