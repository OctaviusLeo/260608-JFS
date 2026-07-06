package com.theblind.todo.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

// Page object for the dashboard page (/dashboard).
// Keeps all selectors in one place so step definitions don't deal with HTML.
public class DashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Input and button for primary tasks
    private final By addPrimaryTaskButton      = By.cssSelector("button.btn-primary");
    private final By primaryTaskTextInput      = By.cssSelector("input.context-input");
    private final By primaryTask               = By.cssSelector("li.task-node");

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Opens the dashboard page and waits for the create task input field to appear
    public void navigateTo(String baseUrl) {
        driver.get(baseUrl + "/dashboard");
        wait.until(ExpectedConditions.visibilityOfElementLocated(primaryTaskTextInput));
    }

    // Ensures that the dashboard page is fully loaded
    public boolean ensurePageIsLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(primaryTaskTextInput));
        return driver.getCurrentUrl().contains("/dashboard");
    }

    public void enterPrimaryTaskText(String text) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(primaryTaskTextInput));
        WebElement field = driver.findElement(primaryTaskTextInput);
        field.clear();
        field.sendKeys(text);
    }

    public void clickCreateTaskButton() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(addPrimaryTaskButton));
        driver.findElement(addPrimaryTaskButton).click();
        // Wait for the task list to update after the async POST + reload completes.
        wait.until(ExpectedConditions.visibilityOfElementLocated(primaryTask));
    }

    public String getPrimaryTaskText() {
        // Wait for the task content span specifically — li.task-node also contains
        // the checkbox, edit/delete buttons, etc., so getText() on the li would
        // return all of that noise. span.task-content holds just the task text.
        By taskContent = By.cssSelector("li.task-node span.task-content");
        wait.until(ExpectedConditions.visibilityOfElementLocated(taskContent));
        return driver.findElement(taskContent).getText().trim();
    }

    // True if the browser is still on /dashboard
    public boolean isOnDashboardPage() {
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        return driver.getCurrentUrl().contains("/dashboard");
    }
}
