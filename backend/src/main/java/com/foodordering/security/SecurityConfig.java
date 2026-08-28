package com.foodordering.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.foodordering.security.ratelimit.LoginRateLimitFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final LoginRateLimitFilter loginRateLimitFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    /*
     * Explicitly allowed origins for CORS.
     * Restricted to known local development and user's Vercel deployment URLs.
     */
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173,http://localhost:3000,https://food-ordering-system-elvis-3170.vercel.app,https://food-ordering-system-git-main-elvis-3170.vercel.app,https://*-elvis-3170.vercel.app}")
    private String allowedOrigins;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            LoginRateLimitFilter loginRateLimitFilter,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint,
            RestAccessDeniedHandler restAccessDeniedHandler
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.loginRateLimitFilter = loginRateLimitFilter;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
    }

    // =====================================================
    // SECURITY FILTER CHAIN (ADMIN)
    // =====================================================

    @Bean
    @org.springframework.core.annotation.Order(1)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/admin/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> applySecurityHeaders(headers))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasAuthority("ROLE_SUPER_ADMIN")
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .addFilterBefore(loginRateLimitFilter, AuthorizationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // =====================================================
    // SECURITY FILTER CHAIN (MAIN)
    // =====================================================

    @Bean
    @org.springframework.core.annotation.Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> applySecurityHeaders(headers))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // Browser CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public authentication endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // M-Pesa Webhook Callback
                        .requestMatchers(HttpMethod.POST, "/api/payments/mpesa/callback").permitAll()

                        // Public Rider registration
                        .requestMatchers(HttpMethod.POST, "/api/riders/register").permitAll()

                        // Super Admin
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_SUPER_ADMIN")

                        // Restaurant Owner & Super Admin management
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/me").hasAnyAuthority("ROLE_OWNER", "ROLE_SUPER_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/restaurants", "/api/restaurants/**").hasAnyAuthority("ROLE_OWNER", "ROLE_SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/restaurants", "/api/restaurants/**").hasAnyAuthority("ROLE_OWNER", "ROLE_SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/restaurants", "/api/restaurants/**").hasAnyAuthority("ROLE_OWNER", "ROLE_SUPER_ADMIN")

                        // Public browsing
                        .requestMatchers(HttpMethod.GET, "/api/restaurants", "/api/restaurants/categories", "/api/restaurants/{restaurantId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/menu-items/restaurant/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()

                        // Menu Item management
                        .requestMatchers(HttpMethod.POST, "/api/menu-items", "/api/menu-items/**").hasAnyAuthority("ROLE_OWNER", "ROLE_SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/menu-items", "/api/menu-items/**").hasAnyAuthority("ROLE_OWNER", "ROLE_SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/menu-items", "/api/menu-items/**").hasAnyAuthority("ROLE_OWNER", "ROLE_SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/menu-items", "/api/menu-items/**").hasAnyAuthority("ROLE_OWNER", "ROLE_SUPER_ADMIN")

                        // Cart
                        .requestMatchers("/api/cart", "/api/cart/**").hasAuthority("ROLE_CUSTOMER")

                        // Orders
                        .requestMatchers("/api/orders", "/api/orders/**").authenticated()

                        // Location
                        .requestMatchers(HttpMethod.GET, "/api/location/reverse").permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(loginRateLimitFilter, AuthorizationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Applies robust HTTP security headers:
     * - Content Security Policy (CSP)
     * - X-Content-Type-Options (nosniff)
     * - Referrer-Policy (strict-origin-when-cross-origin)
     * - Permissions-Policy (restricted sensor/media access)
     * - Frame Options (clickjacking prevention: DENY)
     * - HSTS (HTTP Strict Transport Security)
     */
    private void applySecurityHeaders(org.springframework.security.config.annotation.web.configurers.HeadersConfigurer<HttpSecurity> headers) {
        headers.contentTypeOptions(Customizer.withDefaults());
        headers.frameOptions(frame -> frame.deny());
        headers.referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN));
        headers.permissionsPolicy(permissions -> permissions.policy("geolocation=(self), camera=(), microphone=(), payment=(self)"));
        headers.contentSecurityPolicy(csp -> csp.policyDirectives(
                "default-src 'self'; " +
                "script-src 'self'; " +
                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                "font-src 'self' https://fonts.gstatic.com data:; " +
                "img-src 'self' data: https: blob:; " +
                "connect-src 'self' https:; " +
                "frame-ancestors 'none'; " +
                "object-src 'none'; " +
                "base-uri 'self'; " +
                "form-action 'self'"
        ));
        headers.httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .maxAgeInSeconds(31536000)
        );
    }

    // =====================================================
    // CORS CONFIGURATION
    // =====================================================

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<LoginRateLimitFilter> loginRateLimitFilterRegistration(LoginRateLimitFilter filter) {
        FilterRegistrationBean<LoginRateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();

        configuration.setAllowedOriginPatterns(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Callback-Secret", "X-Requested-With", "Origin", "Idempotency-Key"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // =====================================================
    // PASSWORD ENCODER & AUTH MANAGER
    // =====================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}