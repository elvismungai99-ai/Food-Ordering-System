package com.foodordering.analytics;

import com.foodordering.analytics.dto.RestaurantAnalyticsDto;
import com.foodordering.common.exception.ForbiddenOperationException;
import com.foodordering.security.JwtUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
public class RestaurantAnalyticsController {

    private final RestaurantAnalyticsService analyticsService;
    private final JwtUtil jwtUtil;

    public RestaurantAnalyticsController(
            RestaurantAnalyticsService analyticsService,
            JwtUtil jwtUtil
    ) {
        this.analyticsService =
                analyticsService;
        this.jwtUtil =
                jwtUtil;
    }

    @GetMapping("/restaurant")
    public ResponseEntity<RestaurantAnalyticsDto>
    getRestaurantAnalytics(
            @RequestHeader("Authorization")
            String authHeader
    ) {

        requireRestaurantOwner(authHeader);

        return ResponseEntity.ok(
                analyticsService
                        .getMyRestaurantAnalytics(
                                extractUserId(authHeader)
                        )
        );
    }

    private UUID extractUserId(
            String authHeader
    ) {

        return jwtUtil.extractUserId(
                authHeader.substring(7)
        );
    }

    private void requireRestaurantOwner(
            String authHeader
    ) {

        if (
                authHeader == null
                || !authHeader.startsWith("Bearer ")
        ) {
            throw new ForbiddenOperationException(
                    "Authorization token is missing or invalid"
            );
        }

        String role =
                normalizeRole(
                        jwtUtil.extractRole(
                                authHeader.substring(7)
                        )
                );

        if (
                !"OWNER".equals(role)
                && !"RESTAURANT_ADMIN".equals(role)
                && !"RESTAURANT_OWNER".equals(role)
                && !"SUPER_ADMIN".equals(role)
        ) {
            throw new ForbiddenOperationException(
                    "Only restaurant owners can view restaurant analytics"
            );
        }
    }

    private String normalizeRole(
            String role
    ) {

        if (role == null || role.isBlank()) {
            return "";
        }

        String normalized =
                role.trim().toUpperCase(Locale.ROOT);

        if (normalized.startsWith("ROLE_")) {
            normalized =
                    normalized.substring(5);
        }

        return normalized;
    }
}
