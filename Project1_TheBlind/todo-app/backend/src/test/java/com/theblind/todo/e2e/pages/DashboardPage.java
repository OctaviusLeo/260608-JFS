package com.theblind.todo.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.Keys;

import java.time.Duration;

// Page object for the dashboard page (/dashboard).
// Keeps all selectors in one place so step definitions don't deal with HTML.
public class DashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Input and button for primary tasks
    private final By addPrimaryTaskButton  = By.cssSelector("button.btn-primary");
    private final By primaryTaskTextInput  = By.cssSelector("input.context-input");
    private final By primaryTask           = By.cssSelector("li.task-node");
    private final By primaryTaskContent    = By.cssSelector("li.task-node span.task-content");
    private final By primaryTaskContentEdittable    = By.className("task-edit-input");
    private final By editTaskButton    = By.cssSelector("[title='Rename_task']");
    private final By deleteTaskButton    = By.cssSelector("[title='Delete_task']");
    private final By logoutButton    = By.className("btn-logout");

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(5));
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

    // Logout and return to login page
    public void logout() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(logoutButton));
        driver.findElement(logoutButton).click();
    }

    /**
     * Clicks Add and waits for a task item to appear in the list.
     * Use for scenarios where valid input is submitted and a task is expected to be created.
     */
    public void enterPrimaryTaskText(String text) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(primaryTaskTextInput));
        WebElement field = driver.findElement(primaryTaskTextInput);
        field.clear();
        field.sendKeys(text);
    }

    /**
     * Clicks Add and waits for a task item to appear in the list.
     * Use for scenarios where valid input is submitted and a task is expected to be created.
     */
    public void clickCreateTaskButton() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(addPrimaryTaskButton));
        driver.findElement(addPrimaryTaskButton).click();
        // Block until the async POST + list reload finishes and a task node is visible.
        wait.until(ExpectedConditions.visibilityOfElementLocated(primaryTask));
    }

    /**
     * Deletes first task on dashboard.
     */
    public void clickDeleteTaskButton() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(deleteTaskButton));
        driver.findElement(deleteTaskButton).click();
        //try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        wait.until(ExpectedConditions.stalenessOf(driver.findElement(primaryTask)));
    }

    /**
     * Clicks Add without waiting for a task node to appear afterward.
     * Use for scenarios where blank input is submitted and no task is expected.
     */
    public void clickCreateTaskButtonNoWait() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(addPrimaryTaskButton));
        driver.findElement(addPrimaryTaskButton).click();
        // Short pause to allow any accidental POST to complete before asserting nothing changed.
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }

    /**
     * Returns the text content of the first task in the list.
     * Uses span.task-content to avoid capturing checkbox/button text from the li.
     */
    public String getPrimaryTaskText() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(primaryTaskContent));
        return driver.findElement(primaryTaskContent).getText().trim();
    }

    /**
     * Edits text content of first task in the list.
     * Assumes task already exists.
     * Uses span.task-content to avoid capturing checkbox/button text from the li.
     */
    public void editPrimaryTaskText(String text) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(primaryTaskContentEdittable));
        WebElement updateField = driver.findElement(primaryTaskContentEdittable);
        int textLength = updateField.getAttribute("value").length();

        for (int i = 0; i < textLength; i++) {
            updateField.sendKeys(Keys.BACK_SPACE);
        }

        updateField.sendKeys(text);
        updateField.sendKeys(Keys.ENTER);
    }

    /**
     * Clicks the edit button of a task
     */
    public void clickPrimaryTaskEditButton() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(editTaskButton));
        WebElement updateButton = driver.findElement(editTaskButton);
        updateButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(primaryTaskContentEdittable));
    }


    /**
     * Returns true when no task nodes are present in the DOM.
     * Used to assert that a blank submit did not create a task.
     */
    public boolean checkIfNoPrimaryTask() {
        return driver.findElements(primaryTask).isEmpty();
    }

    public boolean checkIfPrimaryTaskHasText(String text) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(primaryTaskContentEdittable));
        return driver.findElement(primaryTaskContentEdittable).getAttribute("value") == text;
    }

    // True if the browser is on /dashboard
    public boolean isOnDashboardPage() {
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        return driver.getCurrentUrl().contains("/dashboard");
    }
}
