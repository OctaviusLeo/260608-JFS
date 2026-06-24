package com.theblind.todo.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;

/**
 * JWTAuthenticationFilter intercepts every incoming HTTP request exactly once
 * (via OncePerRequestFilter) and validates the JWT Bearer token found
 * in the Authorization header.
 *
 * If a valid token is present, the filter:
 *      - Extracts the username from the token.
 *      - Loads the matching {@link UserDetails} from the database.
 *      - Validates the token against those details.
 *      - Sets the authenticated principal in the SecurityContextHolder
 *          so that downstream filters and controllers see an authenticated user.
 * 
 *
 * If the header is missing, malformed, or the token is invalid/expired, the
 * request continues unauthenticated and Spring Security handles authorization
 * normally (e.g. returning 401/403 for protected routes).
 */
@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    /** Delegates unhandled exceptions to Spring MVC's exception resolution pipeline. */
    private final HandlerExceptionResolver handlerExceptionResolver;

    /** Service responsible for JWT creation, parsing, and validation. */
    private final JWTConfig jwtConfig;

    /** Loads user details by username for token validation. */
    private final UserDetailsService userDetailsService;

    public JWTAuthenticationFilter(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver,
            JWTConfig jwtConfig,
            UserDetailsService userDetailsService
    ) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.jwtConfig = jwtConfig;
        this.userDetailsService = userDetailsService;
    }
    /**
     * Core filter logic. Runs once per request to authenticate the caller via JWT.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response
     * @param filterChain the remaining filter chain to invoke after this filter
     * 
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        // Retrieve the Authorization header from the request
        final String authHeader = request.getHeader("Authorization");

        // If the header is missing or doesn't carry a Bearer token, skip JWT processing
        // and pass the request along — Spring Security will enforce access rules downstream
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Strip the "Bearer " prefix to get the raw JWT string
            final String jwt = authHeader.substring(7);

            // Extract the username claim embedded in the token
            final String username = jwtConfig.extractUsername(jwt);

            // Check whether the SecurityContext already holds an authenticated principal.
            // If it does, there is no need to re-authenticate for this request.
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (username != null && authentication == null) {
                // Load the full UserDetails from the database using the username from the token
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Validate the token: checks signature, expiry, and username match
                if (jwtConfig.isTokenValid(jwt, userDetails)) {
                    // Build a Spring Security authentication token with the user's authorities.
                    // Credentials are set to null because we trust the JWT — no password needed here.
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    // Attach request-specific details (e.g. remote IP) to the authentication object
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Store the authenticated principal in the SecurityContext for this request
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            // Continue down the filter chain with the (now possibly authenticated) context
            filterChain.doFilter(request, response);

        } catch (Exception exception) {
            // Delegate any JWT parsing or authentication errors to Spring MVC's
            // exception resolver so they are handled consistently (e.g. returning 401)
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }
    }
}