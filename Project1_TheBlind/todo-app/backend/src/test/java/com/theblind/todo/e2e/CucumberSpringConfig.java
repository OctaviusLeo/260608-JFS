package com.theblind.todo.e2e;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

// Boots the full Spring app on a random port before each Cucumber run.
// This lets step definitions @Autowire Spring beans (repos, services, etc.).
// Uses the test profile so H2 is used instead of SQLite.
//
// IMPORTANT: the port is fixed at 8080 (DEFINED_PORT) so the Angular dev
// server proxy (proxy.conf.json → target: http://localhost:8080) routes
// browser API calls to this test backend. RANDOM_PORT would break E2E tests
// because the browser would hit the proxy target (8080) while the backend
// listens elsewhere.
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
public class CucumberSpringConfig {
}
