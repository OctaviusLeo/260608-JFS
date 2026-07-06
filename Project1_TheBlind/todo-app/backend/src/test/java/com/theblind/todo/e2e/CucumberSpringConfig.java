package com.theblind.todo.e2e;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

// Boots the full Spring app on a random port before each Cucumber run.
// This lets step definitions @Autowire Spring beans (repos, services, etc.).
// Uses the test profile so H2 is used instead of SQLite.
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
public class CucumberSpringConfig {
}
