package com.theblind.todo.e2e;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

// Entry point for all Cucumber E2E tests.
// Picks up every .feature file under src/test/resources/features/ automatically.
// To add new tests, just drop a .feature file in that folder — no changes needed here.
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
        key   = GLUE_PROPERTY_NAME,
        value = "com.theblind.todo.e2e.steps,com.theblind.todo.e2e")
@ConfigurationParameter(
        key   = PLUGIN_PROPERTY_NAME,
        value = "pretty,html:build/reports/cucumber/cucumber-report.html,json:build/reports/cucumber/cucumber-report.json")
public class CucumberRunnerTest {
}
