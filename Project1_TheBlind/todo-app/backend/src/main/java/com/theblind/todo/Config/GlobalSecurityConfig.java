package com.theblind.todo.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*
 * GlobalSecurityConfig wires the stateless JWT security model:
 *   - CSRF is disabled because the API is token-based (no cookies/sessions).
 *   - Authentication endpoints (registration + login) are publicly reachable.
 *   - Every other request requires a valid JWT, enforced by JwtAuthFilter.
 *   - Sessions are STATELESS; authentication is carried by the bearer token.
 */
@Configuration
@EnableWebSecurity
public class GlobalSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    public GlobalSecurityConfig(JwtAuthFilter jwtAuthFilter, AuthenticationProvider authenticationProvider) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public authentication endpoints (login + registration).
                .requestMatchers("/api/auth/**", "/api/register").permitAll()
                // Permit the error dispatch so unhandled errors surface their real
                // status (e.g. 400/500) instead of being masked as 403 when the
                // container forwards to /error.
                .requestMatchers("/error").permitAll()
                // Everything else requires a valid JWT.
                .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
