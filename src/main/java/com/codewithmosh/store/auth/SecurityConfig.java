package com.codewithmosh.store.auth;

import com.codewithmosh.store.common.SecurityRules;
import com.krd.auth.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration for the application.
 * Extends the base security configuration from krd-spring-starters with:
 * - CORS configuration for React frontend
 * - Domain-specific security rules
 * - Custom exception handling
 *
 * Base configuration (from auth-starter) provides:
 * - JWT authentication filter
 * - Password encoder (BCrypt)
 * - Authentication provider
 * - Authentication manager
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final List<SecurityRules> securityRules;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow requests from React dev server
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        // Allow common HTTP methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow headers including Authorization for JWT
        configuration.setAllowedHeaders(List.of("*"));

        // CRITICAL: Allow credentials (cookies for refresh token)
        configuration.setAllowCredentials(true);

        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Override the default security filter chain to add domain-specific rules.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Stateless sessions (token-based authentication)
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Disable CSRF (not needed for REST APIs)
                .csrf(AbstractHttpConfigurer::disable)

                // Enable CORS with our custom configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Authorize HTTP requests with domain-specific rules
                .authorizeHttpRequests(c -> {
                    // Apply domain-specific security rules
                    securityRules.forEach(rule -> rule.configure(c));

                    // Allow actuator endpoints and require authentication for all other requests
                    c.requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                     .anyRequest().authenticated();
                })

                // Add JWT authentication filter from auth-starter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Custom exception handling
                .exceptionHandling(c -> {
                    c.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
                    c.accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                    });
                });

        return http.build();
    }
}
