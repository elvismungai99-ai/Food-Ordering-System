package com.foodordering.security.ratelimit;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodordering.common.error.ApiErrorResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.core.Ordered;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class LoginRateLimitFilter
        extends OncePerRequestFilter
        implements Ordered {

    private final LoginRateLimitService
            loginRateLimitService;

    private final ObjectMapper objectMapper;

    private final boolean ignoreLocalhost;


    public LoginRateLimitFilter(
            LoginRateLimitService loginRateLimitService,
            ObjectMapper objectMapper,
            @Value("${security.rate-limit.ignore-localhost:false}")
            boolean ignoreLocalhost
    ) {

        this.loginRateLimitService =
                loginRateLimitService;

        this.objectMapper =
                objectMapper;

        this.ignoreLocalhost =
                ignoreLocalhost;
    }


    // =====================================================
    // RATE LIMIT FILTER
    // =====================================================

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 5;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        /*
         * Only rate-limit:
         *
         * POST /api/auth/login
         */
        if (!isLoginRequest(request)) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String clientIp =
                getClientIpAddress(
                        request
                );

        if (
                ignoreLocalhost
                && isLocalhost(clientIp)
        ) {
            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        LoginRateLimitService.RateLimitResult result =
                loginRateLimitService
                        .checkRequest(
                                clientIp
                        );


        /*
         * Useful informational headers.
         */
        response.setHeader(
                "X-RateLimit-Limit",
                "5"
        );

        response.setHeader(
                "X-RateLimit-Remaining",
                String.valueOf(
                        result.remaining()
                )
        );


        // =================================================
        // REQUEST ALLOWED
        // =================================================

        if (result.allowed()) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        // =================================================
        // REQUEST BLOCKED
        // =================================================

        response.setStatus(
                HttpStatus
                        .TOO_MANY_REQUESTS
                        .value()
        );

        response.setContentType(
                MediaType
                        .APPLICATION_JSON_VALUE
        );

        response.setHeader(
                "Retry-After",
                String.valueOf(
                        result.retryAfterSeconds()
                )
        );

        ApiErrorResponse errorResponse =
                new ApiErrorResponse(
                        LocalDateTime.now(ZoneId.systemDefault()).toString(),
                        HttpStatus
                                .TOO_MANY_REQUESTS
                                .value(),
                        "Too many requests",
                        "You have used all your login attempts. Please wait before trying again, or use Forgot password to reset your password.",
                        request.getRequestURI(),
                        Map.of()
                );

        objectMapper.writeValue(
                response.getOutputStream(),
                errorResponse
        );
    }


    // =====================================================
    // IDENTIFY LOGIN REQUEST
    // =====================================================

    private boolean isLoginRequest(
            HttpServletRequest request
    ) {

        String path =
                request.getServletPath();

        if (
                path == null
                || path.isBlank()
        ) {
            path =
                    request.getRequestURI();
        }

        return "POST".equalsIgnoreCase(
                request.getMethod()
        )
                && "/api/auth/login".equals(
                        path
                );
    }


    // =====================================================
    // GET CLIENT IP
    // =====================================================

    private boolean isLocalhost(String clientIp) {
        return "127.0.0.1".equals(clientIp)
                || "0:0:0:0:0:0:0:1".equals(clientIp)
                || "::1".equals(clientIp)
                || "localhost".equalsIgnoreCase(clientIp);
    }

    private String getClientIpAddress(
            HttpServletRequest request
    ) {

        /*
         * During local development this will
         * usually return:
         *
         * 127.0.0.1
         *
         * or
         *
         * 0:0:0:0:0:0:0:1
         */

        String forwardedFor =
                request.getHeader(
                        "X-Forwarded-For"
                );

        if (
                forwardedFor != null
                && !forwardedFor.isBlank()
        ) {

            return forwardedFor
                    .split(",")[0]
                    .trim();
        }

        return request
                .getRemoteAddr();
    }
}
