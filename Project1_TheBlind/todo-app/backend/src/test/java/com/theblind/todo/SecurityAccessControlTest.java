package com.theblind.todo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration test for the server-side JWT access-control remediation (task 3.1).
 *
 * <p>Loads the full Spring Boot application context (including the
 * {@code SecurityFilterChain} and {@code JwtAuthFilter}) against an in-memory H2
 * database via the {@code test} profile, so the real security filter chain is
 * exercised end to end.
 *
 * <p>Validates Requirement 6.2: a protected endpoint called without a valid JWT
 * is rejected with an unauthorized response, while the public authentication
 * endpoints remain reachable (not blocked by the security filter).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAccessControlTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Req 6.2 (protected): GET /api/tasks without an Authorization header must be
     * rejected by the security filter chain with 401 (Unauthorized) or 403 (Forbidden).
     */
    @Test
    void getTasksWithoutToken_isRejectedAsUnauthorized() throws Exception {
        int status = mockMvc.perform(get("/api/tasks"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(status)
                .as("GET /api/tasks without a token must be rejected with 401 or 403")
                .isIn(401, 403);
    }

    /**
     * Req 6.2 (public): the live registration endpoint (/api/register) must remain
     * reachable without a token. A malformed/empty payload may yield a 4xx from the
     * controller, but the request must NOT be blocked by the security filter as
     * unauthorized (401) or forbidden (403).
     */
    @Test
    void registerWithoutToken_isNotBlockedBySecurity() throws Exception {
        int status = mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demoUser\",\"password\":\"Demo12!@\"}"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(status)
                .as("/api/register must be permitted without a token (not 401/403)")
                .isNotIn(401, 403);
    }

    /**
     * Req 6.2 (public): the login endpoint (/api/auth/login) must remain reachable
     * without a token. Bad credentials produce a 4xx from the controller, but the
     * request must NOT be blocked by the security filter as unauthorized/forbidden.
     */
    @Test
    void loginWithoutToken_isNotBlockedBySecurity() throws Exception {
        int status = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nobody\",\"password\":\"WrongPass1!@\"}"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(status)
                .as("/api/auth/login must be permitted without a token (not 401/403)")
                .isNotIn(401, 403);
    }
}
