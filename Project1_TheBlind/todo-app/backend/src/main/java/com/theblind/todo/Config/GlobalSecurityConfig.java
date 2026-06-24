package com.theblind.todo.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
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

    private final JWTAuthenticationFilter jwtAuthenticationFilter;

    public GlobalSecurityConfig(JWTAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/register", "/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
