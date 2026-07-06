package com.theblind.todo.e2e.config;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

// Manages the Chrome browser for each scenario.
// Step definitions that need the browser should inject this class and call getDriver().
// Don't add @Component here — Cucumber manages it and adding that annotation causes a duplicate bean error.
public class BrowserConfig {

    private WebDriver driver;

    @BeforeAll
    public static void setupDriverBinary() {
        WebDriverManager.chromedriver().setup();
    }

    @Before
    public void openBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--headless=new",          // no visible window
                "--no-sandbox",            // needed in CI/Docker
                "--disable-dev-shm-usage", // avoids memory issues in CI
                "--disable-gpu",
                "--window-size=1920,1080"
        );
        driver = new ChromeDriver(options);
    }

    @After
    public void closeBrowser() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    // Returns the active WebDriver for the current scenario
    public WebDriver getDriver() {
        return driver;
    }
}
