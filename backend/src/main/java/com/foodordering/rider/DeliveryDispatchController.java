package com.foodordering.rider;

import com.foodordering.common.exception.ForbiddenOperationException;
import com.foodordering.rider.dto.AutoDeliveryRequest;
import com.foodordering.rider.dto.CreateDeliveryRequest;
import com.foodordering.rider.dto.DeliveryRequestDto;
import com.foodordering.rider.dto.RiderDto;
import com.foodordering.security.JwtUtil;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/delivery-dispatch")
public class DeliveryDispatchController {

    private final RiderService riderService;
    private final JwtUtil jwtUtil;

    public DeliveryDispatchController(
            RiderService riderService,
            JwtUtil jwtUtil
    ) {
        this.riderService = riderService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/available-riders")
    public ResponseEntity<List<RiderDto>> getAvailableRiders(
            @RequestHeader("Authorization")
            String authHeader
    ) {

        requireRestaurantOwner(authHeader);

        return ResponseEntity.ok(
                riderService.getAvailableRiders()
        );
    }

    @GetMapping("/requests")
    public ResponseEntity<List<DeliveryRequestDto>> getRestaurantRequests(
            @RequestHeader("Authorization")
            String authHeader
    ) {

        requireRestaurantOwner(authHeader);

        return ResponseEntity.ok(
                riderService
                        .getRestaurantDeliveryRequests(
                                extractUserId(authHeader)
                        )
        );
    }

    @PostMapping("/requests")
    public ResponseEntity<DeliveryRequestDto> createDeliveryRequest(
            @RequestHeader("Authorization")
            String authHeader,

            @Valid
            @RequestBody
            CreateDeliveryRequest request
    ) {

        requireRestaurantOwner(authHeader);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        riderService.createDeliveryRequest(
                                extractUserId(authHeader),
                                request
                        )
                );
    }

    @PostMapping("/requests/auto")
    public ResponseEntity<DeliveryRequestDto> createAutomaticDeliveryRequest(
            @RequestHeader("Authorization")
            String authHeader,

            @Valid
            @RequestBody
            AutoDeliveryRequest request
    ) {

        requireRestaurantOwner(authHeader);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        riderService.createAutomaticDeliveryRequest(
                                extractUserId(authHeader),
                                request
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

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication != null
                && authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                isRestaurantOwnerRole(
                                        authority.getAuthority()
                                )
                        )
        ) {
            return;
        }

        String role =
                extractRole(authHeader);

        if (!isRestaurantOwnerRole(role)) {
            throw new ForbiddenOperationException(
                    "Only restaurant owners can dispatch delivery requests"
            );
        }
    }

    private String extractRole(
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
                jwtUtil.extractRole(
                        authHeader.substring(7)
                );

        if (role == null || role.isBlank()) {
            return "";
        }

        String normalized =
                role.trim().toUpperCase(Locale.ROOT);

        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }

        return normalized;
    }

    private boolean isRestaurantOwnerRole(
            String role
    ) {

        String normalized =
                normalizeRole(role);

        return "OWNER".equals(normalized)
                || "RESTAURANT_ADMIN".equals(normalized)
                || "RESTAURANT_OWNER".equals(normalized)
                || "ADMIN_RESTAURANT".equals(normalized)
                || "SUPER_ADMIN".equals(normalized);
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
