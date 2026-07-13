package com.foodordering.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // Public authentication endpoints
                        .requestMatchers(
                                "/api/auth/**"
                        )
                        .permitAll()

                        // Public restaurant browsing endpoints
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/restaurants/**"
                        )
                        .permitAll()

                        // Public menu browsing endpoints
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/menu-items/**"
                        )
                        .permitAll()

                        /*
                         * Cart endpoints.
                         *
                         * These should eventually require a valid JWT.
                         * During development, the controller still reads
                         * the user ID from the Authorization header.
                         */
                        .requestMatchers(
                                "/api/cart/**"
                        )
                        .permitAll()

                        // Swagger documentation endpoints
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        )
                        .permitAll()

                        // Spring Boot actuator endpoints
                        .requestMatchers(
                                "/actuator/**"
                        )
                        .permitAll()

                        /*
                         * Temporary development configuration.
                         *
                         * Keep this as permitAll until your JWT filter
                         * and custom authentication provider are fully
                         * connected to Spring Security.
                         */
                        .anyRequest()
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:5173"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}