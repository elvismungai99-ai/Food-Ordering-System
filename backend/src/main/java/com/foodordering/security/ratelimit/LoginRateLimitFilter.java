package com.foodordering.security.ratelimit;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodordering.common.error.ApiErrorResponse;
import com.foodordering.security.ClientIpResolver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class LoginRateLimitFilter
        extends OncePerRequestFilter
        implements Ordered {

    private final LoginRateLimitService loginRateLimitService;
    private final ObjectMapper objectMapper;
    private final boolean ignoreLocalhost;
    private final ClientIpResolver clientIpResolver;
    private final EndpointRateLimiter endpointRateLimiter;

    @Autowired
    public LoginRateLimitFilter(
            LoginRateLimitService loginRateLimitService,
            ObjectMapper objectMapper,
            @Value("${security.rate-limit.ignore-localhost:false}")
            boolean ignoreLocalhost,
            @Autowired(required = false) ClientIpResolver clientIpResolver,
            @Autowired(required = false) EndpointRateLimiter endpointRateLimiter
    ) {
        this.loginRateLimitService = loginRateLimitService;
        this.objectMapper = objectMapper;
        this.ignoreLocalhost = ignoreLocalhost;
        this.clientIpResolver = clientIpResolver != null ? clientIpResolver : new ClientIpResolver();
        this.endpointRateLimiter = endpointRateLimiter;
    }

    public LoginRateLimitFilter(
            LoginRateLimitService loginRateLimitService,
            ObjectMapper objectMapper,
            boolean ignoreLocalhost
    ) {
        this(loginRateLimitService, objectMapper, ignoreLocalhost, new ClientIpResolver(), null);
    }

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

        String path = request.getServletPath();
        if (path == null || path.isBlank()) {
            path = request.getRequestURI();
        }
        String method = request.getMethod();

        String clientIp = getClientIpAddress(request);

        if (ignoreLocalhost && isLocalhost(clientIp)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1. POST /api/auth/login
        if ("POST".equalsIgnoreCase(method) && "/api/auth/login".equals(path)) {
            LoginRateLimitService.RateLimitResult result = loginRateLimitService.checkRequest(clientIp);
            handleRateLimitResult(request, response, filterChain, result.allowed(), result.remaining(), 5,
                    result.retryAfterSeconds(), "You have used all your login attempts. Please wait before trying again.");
            return;
        }

        // 2. Additional sensitive endpoints
        if (endpointRateLimiter != null) {
            // POST /api/auth/register (5 per 10 mins)
            if ("POST".equalsIgnoreCase(method) && "/api/auth/register".equals(path)) {
                var res = endpointRateLimiter.checkRateLimit("register", clientIp, 5, Duration.ofMinutes(10));
                if (!res.allowed()) {
                    blockRequest(request, response, res.retryAfterSeconds(), "Too many registration attempts. Please wait before creating another account.");
                    return;
                }
            }

            // POST /api/auth/forgot-password (3 per 15 mins)
            if ("POST".equalsIgnoreCase(method) && "/api/auth/forgot-password".equals(path)) {
                var res = endpointRateLimiter.checkRateLimit("forgot-password", clientIp, 3, Duration.ofMinutes(15));
                if (!res.allowed()) {
                    blockRequest(request, response, res.retryAfterSeconds(), "Too many password reset requests. Please check your email or try again later.");
                    return;
                }
            }

            // POST /api/auth/reset-password (5 per 15 mins)
            if ("POST".equalsIgnoreCase(method) && "/api/auth/reset-password".equals(path)) {
                var res = endpointRateLimiter.checkRateLimit("reset-password", clientIp, 5, Duration.ofMinutes(15));
                if (!res.allowed()) {
                    blockRequest(request, response, res.retryAfterSeconds(), "Too many password reset attempts. Please wait before trying again.");
                    return;
                }
            }

            // POST /api/orders (15 checkout attempts per 1 min)
            if ("POST".equalsIgnoreCase(method) && "/api/orders".equals(path)) {
                var res = endpointRateLimiter.checkRateLimit("checkout", clientIp, 15, Duration.ofMinutes(1));
                if (!res.allowed()) {
                    blockRequest(request, response, res.retryAfterSeconds(), "Too many order requests. Please wait a moment before placing another order.");
                    return;
                }
            }

            // POST /api/orders/*/retry-payment (5 payment retry attempts per 1 min)
            if ("POST".equalsIgnoreCase(method) && path.startsWith("/api/orders/") && path.endsWith("/retry-payment")) {
                var res = endpointRateLimiter.checkRateLimit("retry-payment", clientIp, 5, Duration.ofMinutes(1));
                if (!res.allowed()) {
                    blockRequest(request, response, res.retryAfterSeconds(), "Too many payment retries. Please wait a moment before retrying.");
                    return;
                }
            }

            // POST /api/reviews (10 reviews per 1 min)
            if ("POST".equalsIgnoreCase(method) && "/api/reviews".equals(path)) {
                var res = endpointRateLimiter.checkRateLimit("review", clientIp, 10, Duration.ofMinutes(1));
                if (!res.allowed()) {
                    blockRequest(request, response, res.retryAfterSeconds(), "Too many review submissions. Please wait a moment.");
                    return;
                }
            }

            // Sensitive admin actions (PATCH /api/admin/**, DELETE /api/admin/**) - 30 per 1 min
            if (("PATCH".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) && path.startsWith("/api/admin/")) {
                var res = endpointRateLimiter.checkRateLimit("admin-action", clientIp, 30, Duration.ofMinutes(1));
                if (!res.allowed()) {
                    blockRequest(request, response, res.retryAfterSeconds(), "Admin rate limit exceeded. Please wait before performing further actions.");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void handleRateLimitResult(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain,
            boolean allowed,
            int remaining,
            int limit,
            long retryAfterSeconds,
            String message
    ) throws ServletException, IOException {

        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));

        if (allowed) {
            filterChain.doFilter(request, response);
            return;
        }

        blockRequest(request, response, retryAfterSeconds, message);
    }

    private void blockRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            long retryAfterSeconds,
            String message
    ) throws IOException {

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                LocalDateTime.now(ZoneId.systemDefault()).toString(),
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Too many requests",
                message,
                request.getRequestURI(),
                Map.of()
        );

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    private boolean isLocalhost(String clientIp) {
        if (clientIpResolver != null) {
            return clientIpResolver.isLocalhost(clientIp);
        }
        return "127.0.0.1".equals(clientIp) || "0:0:0:0:0:0:0:1".equals(clientIp) || "::1".equals(clientIp) || "localhost".equalsIgnoreCase(clientIp);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        if (clientIpResolver != null) {
            return clientIpResolver.resolveClientIp(request);
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
