package com.codewithmosh.store.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // Stateless sessions (token-based authentication)
            .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Disable CSRF (Cross-Site Request Forgery)
            // Attack where browser gets tricked into making a request on behalf of a user without their knowledge
            // Don't need this in REST APIs
            .csrf(AbstractHttpConfigurer::disable)

            // Authorize different HTTP requests
            .authorizeHttpRequests(c -> c
                    .requestMatchers("/carts/**").permitAll()
                    .requestMatchers(HttpMethod.POST,"/users").permitAll()
                    .anyRequest().authenticated()
            );

        // Returns a Security Filter Chain object that spring will use to secure HTTP requests sent to this server
        return http.build();
    }
}
