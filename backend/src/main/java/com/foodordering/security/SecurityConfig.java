package com.foodordering.security;

import java.util.List;

import org.springframework.boot.web.servlet.FilterRegistrationBean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.foodordering.security.ratelimit.LoginRateLimitFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter
            jwtAuthenticationFilter;

    private final LoginRateLimitFilter
            loginRateLimitFilter;

    private final RestAuthenticationEntryPoint
            restAuthenticationEntryPoint;

    private final RestAccessDeniedHandler
            restAccessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            LoginRateLimitFilter loginRateLimitFilter,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint,
            RestAccessDeniedHandler restAccessDeniedHandler
    ) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;

        this.loginRateLimitFilter =
                loginRateLimitFilter;

        this.restAuthenticationEntryPoint =
                restAuthenticationEntryPoint;

        this.restAccessDeniedHandler =
                restAccessDeniedHandler;
    }

    // =====================================================
    // SECURITY FILTER CHAIN
    // =====================================================

    @Bean
    @org.springframework.core.annotation.Order(1)
    public SecurityFilterChain adminSecurityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .securityMatcher(
                        "/api/admin/**"
                )
                .cors(cors -> {
                })
                .csrf(csrf ->
                        csrf.disable()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest()
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    @org.springframework.core.annotation.Order(2)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // =========================================
                // CORS
                // =========================================

                .cors(cors -> {
                })

                // =========================================
                // CSRF
                // =========================================

                /*
                 * This is a stateless JWT REST API.
                 */
                .csrf(csrf ->
                        csrf.disable()
                )

                // =========================================
                // SESSION
                // =========================================

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(
                                restAuthenticationEntryPoint
                        )
                        .accessDeniedHandler(
                                restAccessDeniedHandler
                        )
                )

                // =========================================
                // AUTHORIZATION
                // =========================================

                .authorizeHttpRequests(auth -> auth

                        /*
                         * Browser CORS preflight.
                         */
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        )
                        .permitAll()

                        /*
                         * IMPORTANT:
                         *
                         * Login and registration must
                         * remain public.
                         */
                        .requestMatchers(
                                "/api/auth/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/riders/register"
                        )
                        .permitAll()

                        // =================================
                        // SUPER ADMIN
                        // =================================

                        .requestMatchers(
                                "/api/admin/**"
                        )
                        .permitAll(
                        )

                        // =================================
                        // RESTAURANT OWNER
                        // =================================

                        /*
                         * The currently authenticated
                         * OWNER loads their own restaurant.
                         *
                         * Keep this BEFORE broader
                         * restaurant rules.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/restaurants/me"
                        )
                        .permitAll()

                        /*
                         * Restaurant creation.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/restaurants",
                                "/api/restaurants/**"
                        )
                        .hasAnyAuthority(
                                "ROLE_OWNER",
                                "ROLE_SUPER_ADMIN"
                        )

                        /*
                         * Restaurant update.
                         */
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/restaurants",
                                "/api/restaurants/**"
                        )
                        .hasAnyAuthority(
                                "ROLE_OWNER",
                                "ROLE_SUPER_ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/restaurants",
                                "/api/restaurants/**"
                        )
                        .hasAnyAuthority(
                                "ROLE_OWNER",
                                "ROLE_SUPER_ADMIN"
                        )

                        // =================================
                        // PUBLIC RESTAURANT BROWSING
                        // =================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/restaurants",
                                "/api/restaurants/categories",
                                "/api/restaurants/{restaurantId}"
                        )
                        .permitAll()

                        // =================================
                        // PUBLIC MENU VIEWING
                        // =================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/menu-items/restaurant/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/reviews/**"
                        )
                        .permitAll()

                        // =================================
                        // MENU MANAGEMENT
                        // =================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/menu-items",
                                "/api/menu-items/**"
                        )
                        .hasAnyAuthority(
                                "ROLE_OWNER",
                                "ROLE_SUPER_ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/menu-items",
                                "/api/menu-items/**"
                        )
                        .hasAnyAuthority(
                                "ROLE_OWNER",
                                "ROLE_SUPER_ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/menu-items",
                                "/api/menu-items/**"
                        )
                        .hasAnyAuthority(
                                "ROLE_OWNER",
                                "ROLE_SUPER_ADMIN"
                        )

                        // =================================
                        // CUSTOMER CART
                        // =================================

                        .requestMatchers(
                                "/api/cart",
                                "/api/cart/**"
                        )
                        .permitAll(
                        )

                        // =================================
                        // ORDERS
                        // =================================

                        .requestMatchers(
                                "/api/orders",
                                "/api/orders/**"
                        )
                        .permitAll(
                        )

                        // =================================
                        // LIVE DELIVERY LOCATION
                        // =================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/location/reverse"
                        )
                        .permitAll()

                        // =================================
                        // EVERYTHING ELSE
                        // =================================

                        .anyRequest()
                        .authenticated()
                )

                // =========================================

                // JWT FILTER
                // =========================================

                .addFilterBefore(
                        loginRateLimitFilter,
                        AuthorizationFilter.class
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // =====================================================
    // CORS
    // =====================================================

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter>
    jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter filter
    ) {

        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(
                        filter
                );

        registration.setEnabled(
                false
        );

        return registration;
    }

    @Bean
    public FilterRegistrationBean<LoginRateLimitFilter>
    loginRateLimitFilterRegistration(
            LoginRateLimitFilter filter
    ) {

        FilterRegistrationBean<LoginRateLimitFilter> registration =
                new FilterRegistrationBean<>(
                        filter
                );

        registration.setEnabled(
                false
        );

        return registration;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        /*
         * React/Vite development frontend.
         */
        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173"
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
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept"
                )
        );

        configuration.setAllowCredentials(
                true
        );

        configuration.setMaxAge(
                3600L
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    // =====================================================
    // PASSWORD ENCODER
    // =====================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    // =====================================================
    // AUTHENTICATION MANAGER
    // =====================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration
                .getAuthenticationManager();
    }
}
