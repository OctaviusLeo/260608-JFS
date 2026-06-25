package com.theblind.todo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
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

    /**
     * Regression: a request carrying a VALID JWT must be authenticated by the
     * filter and reach the controller, returning 200 with a JSON array body.
     *
     * <p>This guards against the filter's collaborators (JWTConfig /
     * UserDetailsService) being left uninjected — previously they were null, so
     * the filter threw a NullPointerException that was swallowed into an empty
     * 200 response (the frontend saw a {@code null} body instead of an array).
     */
    @Test
    void validToken_allowsAccessToProtectedTasksEndpoint() throws Exception {
        String credentials = "{\"username\":\"authFlowUser\",\"password\":\"Demo12!@\"}";

        // Register, then log in to obtain a real JWT for this user.
        mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentials));

        String loginBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract the JWT from the login JSON without a JSON library on the
        // test classpath: {"token":"...", ...}
        Matcher tokenMatcher = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(loginBody);
        assertThat(tokenMatcher.find()).as("login response must contain a token").isTrue();
        String token = tokenMatcher.group(1);
        assertThat(token).as("login must return a non-empty JWT").isNotBlank();

        // Call the protected endpoint with the valid token.
        MockHttpServletResponse response = mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + token))
                .andReturn()
                .getResponse();

        assertThat(response.getStatus())
                .as("GET /api/tasks with a valid token must succeed (200)")
                .isEqualTo(200);
        assertThat(response.getContentAsString().trim())
                .as("the tasks endpoint must return a JSON array, not an empty/null body")
                .startsWith("[");
    }

    /**
     * Regression: a malformed/garbage token must be rejected with 401, NOT
     * silently turned into an empty 200 response.
     */
    @Test
    void malformedToken_isRejectedAsUnauthorized() throws Exception {
        int status = mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer not.a.valid.token"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(status)
                .as("a malformed token must yield 401, never an empty 200")
                .isEqualTo(401);
    }
}
