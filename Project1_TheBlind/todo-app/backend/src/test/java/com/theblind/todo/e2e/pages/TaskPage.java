package com.theblind.todo.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page object for task interactions on the dashboard.
 * Selectors match the Angular frontend's task-item component which uses
 * CSS classes (not data-testid attributes):
 *   li.task-node          — each task item
 *   span.task-content     — displayed task text
 *   input.task-checkbox   — completion checkbox
 *   input.task-edit-input — inline rename input
 *   input.subtask-input   — new subtask input
 *   ul.task-tree          — nested children container
 *   button.btn-ghost      — action buttons (edit, +, ×)
 *   button.btn-primary    — submit buttons (Add task, Add subtask)
 */
public class TaskPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public TaskPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    // ── Navigation ─────────────────────────────────────────────────────────────

    public void navigateTo(String baseUrl) {
        driver.get(baseUrl + "/dashboard");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input.context-input")));
    }

    /**
     * Waits until a task with the given content text is visible on the page.
     * Searches for a span.task-content whose text matches the content.
     */
    public void waitForTaskVisible(String content) {
        String xpath = String.format("//li[contains(@class,'task-node')]//span[contains(@class,'task-content') and normalize-space(text())='%s']", content);
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
    }

    public void refresh() {
        driver.navigate().refresh();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input.context-input")));
    }

    // ── Locators ─────────────────────────────────────────────────────────────

    /**
     * Finds the li.task-node whose DIRECT .task-row > span.task-content matches the text.
     * Uses child (not descendant) axis to avoid matching ancestor task-nodes
     * that contain the same text in a nested subtree.
     */
    private WebElement findTaskByContent(String content) {
        String xpath = String.format(
                "//li[contains(@class,'task-node')][./div[contains(@class,'task-row')]/span[contains(@class,'task-content') and normalize-space(text())='%s']]",
                content);
        return wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
    }

    private WebElement findTaskByContentOrNull(String content) {
        String xpath = String.format(
                "//li[contains(@class,'task-node')][./div[contains(@class,'task-row')]/span[contains(@class,'task-content') and normalize-space(text())='%s']]",
                content);
        List<WebElement> elements = driver.findElements(By.xpath(xpath));
        return elements.isEmpty() ? null : elements.get(0);
    }

    // ── Task creation ────────────────────────────────────────────────────────

    /**
     * Creates a top-level task through the dashboard form (input.context-input + button.btn-primary).
     * Waits for the task to appear in the list after creation.
     */
    public void createTask(String content) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input.context-input")));
        // Don't use .clear() — it triggers blur which fires onRename() and destroys the input.
        // Select-all + type replaces content without losing focus.
        input.sendKeys(org.openqa.selenium.Keys.chord(org.openqa.selenium.Keys.CONTROL, "a"));
        input.sendKeys(content);

        // Click the dashboard-level Add button (direct child of .task-controls, not inside a subtask form)
        driver.findElement(By.cssSelector(".task-controls button.btn-primary")).click();

        // Wait for the new task to appear
        waitForTaskVisible(content);
    }

    /**
     * Adds a child task to a parent task through the UI.
     * Clicks the "+" button on the parent, fills the subtask input, and submits.
     */
    public void addChildTask(String parentContent, String childContent, boolean expectSuccess) {
        WebElement parent = findTaskByContent(parentContent);

        // Click the "+" (add subtask) button — it's the second btn-ghost in .task-actions
        List<WebElement> actionButtons = parent.findElements(By.cssSelector("div.task-row > span.task-actions > button.btn-ghost"));
        // Buttons are: [edit, +, ×]
        WebElement addSubtaskBtn = actionButtons.get(1);
        addSubtaskBtn.click();

        // Wait for the subtask form to open and the input to be visible
        WebElement subtaskInput = wait.until(ExpectedConditions.visibilityOf(
                parent.findElement(By.cssSelector(".subtask-form input.subtask-input"))));
        // Don't use .clear() — it triggers blur which fires onRename() and destroys the input.
        // Select-all + type replaces content without losing focus.
        subtaskInput.sendKeys(org.openqa.selenium.Keys.chord(org.openqa.selenium.Keys.CONTROL, "a"));
        subtaskInput.sendKeys(childContent);

        // Click the "Add" button inside the subtask form
        parent.findElement(By.cssSelector(".subtask-form button.btn-primary")).click();

        if(expectSuccess) {
            // Input has maxlength=50, so only first 50 chars get submitted.
            // Wait for truncated content to appear.
            String expectedContent = childContent.length() > 50 ? childContent.substring(0, 50) : childContent;
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                            String.format("//li[contains(@class,'task-node')]//span[contains(@class,'task-content') and normalize-space(text())='%s']", expectedContent)
                    )));
        }
    }

    // ── Visibility ───────────────────────────────────────────────────────────

    public boolean isChildTaskVisible(String childContent) {
        WebElement task = findTaskByContentOrNull(childContent);
        return task != null && task.isDisplayed();
    }

    public boolean isChildTaskVisibleUnderParent(String parentContent, String childContent) {
        try {
            // Full XPath from root: find a task-node with childContent that lives inside
            // the task-tree of the parent task-node.
            String xpath = String.format(
                    "//li[contains(@class,'task-node')][./div[contains(@class,'task-row')]/span[contains(@class,'task-content') and normalize-space(text())='%s']]//ul[contains(@class,'task-tree')]//li[contains(@class,'task-node')][./div[contains(@class,'task-row')]/span[contains(@class,'task-content') and normalize-space(text())='%s']]",
                    parentContent, childContent);
            WebElement child = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
            return child.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public int getChildTaskCount(String parentContent) {
        WebElement parent = findTaskByContent(parentContent);
        List<WebElement> children = parent.findElements(
                By.cssSelector("ul.task-tree > app-task-item"));
        return children.size();
    }

    // ── Completion ───────────────────────────────────────────────────────────

    public void toggleChildTaskComplete(String childContent) {
        WebElement task = findTaskByContent(childContent);
        task.findElement(By.cssSelector(".task-row > input.task-checkbox")).click();
    }

    public boolean isChildTaskComplete(String childContent) {
        WebElement task = findTaskByContent(childContent);
        WebElement checkbox = task.findElement(By.cssSelector(".task-row > input.task-checkbox"));
        // The Angular template binds [checked]="isComplete()" on the checkbox
        // and adds .completed class to the span.task-content
        WebElement contentSpan = task.findElement(By.cssSelector(".task-row > span.task-content"));
        return checkbox.isSelected()
                || "true".equals(checkbox.getAttribute("checked"))
                || contentSpan.getAttribute("class").contains("completed");
    }

    // ── Editing ──────────────────────────────────────────────────────────────

    /**
     * Renames a task by clicking the edit button, typing new content, and pressing Enter.
     * The frontend switches span.task-content to input.task-edit-input on edit.
     * All queries go through driver (not stored WebElement refs) to avoid stale refs
     * from Angular re-rendering the component tree.
     */
    public void editChildTask(String currentContent, String newContent, boolean expectSuccess) {
        // Locate and click "edit" button using a single XPath from driver
        String editBtnXpath = String.format(
                "//li[contains(@class,'task-node')][./div[contains(@class,'task-row')]/span[contains(@class,'task-content') and normalize-space(text())='%s']]/div[contains(@class,'task-row')]/span[contains(@class,'task-actions')]/button[contains(@class,'btn-ghost')][1]",
                currentContent);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(editBtnXpath))).click();

        // Wait for edit input to appear (Angular swaps span → input)
        WebElement editInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input.task-edit-input")));
        // Don't use .clear() — it triggers blur which fires onRename() and destroys the input.
        // Select-all + type replaces content without losing focus.
        editInput.sendKeys(org.openqa.selenium.Keys.chord(org.openqa.selenium.Keys.CONTROL, "a"));
        editInput.sendKeys(newContent);
        editInput.sendKeys(org.openqa.selenium.Keys.ENTER);

        if(expectSuccess) {
            // Wait for new content span to appear
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                            String.format("//li[contains(@class,'task-node')]//span[contains(@class,'task-content') and normalize-space(text())='%s']", newContent)
                    )));
        }
    }

    // ── Deletion ─────────────────────────────────────────────────────────────

    /**
     * Deletes a task by clicking the "×" button (third btn-ghost).
     * The frontend calls deleteTask immediately with no confirmation dialog.
     */
    public void deleteTask(String content) {
        WebElement task = findTaskByContent(content);

        // Click the "×" (delete) button — third btn-ghost in task-actions
        // Use direct child selectors to stay within this task's own row, not nested children
        List<WebElement> actionButtons = task.findElements(By.cssSelector("div.task-row > span.task-actions > button.btn-ghost"));
        WebElement deleteBtn = actionButtons.get(2);
        deleteBtn.click();

        // Wait for the task to disappear from the DOM
        String xpath = String.format("//li[contains(@class,'task-node')][.//span[contains(@class,'task-content') and normalize-space(text())='%s']]", content);
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(xpath)));
    }

    public boolean taskExists(String content) {
        return findTaskByContentOrNull(content) != null;
    }

    // ── Validation errors ────────────────────────────────────────────────────

    /**
     * Checks if an error message is displayed on the dashboard.
     * The dashboard shows errors in a p.status.error element.
     */
    public boolean hasValidationError() {
        try {
            WebElement error = new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector("p.status.error")));
            return error.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
