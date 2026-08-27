package com.foodordering.rider;

import com.foodordering.auth.AuthResponse;
import com.foodordering.common.exception.ForbiddenOperationException;
import com.foodordering.rider.dto.DeliveryRequestDto;
import com.foodordering.rider.dto.RejectDeliveryRequest;
import com.foodordering.rider.dto.RiderAvailabilityRequest;
import com.foodordering.rider.dto.RiderDashboardDto;
import com.foodordering.rider.dto.RiderDto;
import com.foodordering.rider.dto.RiderLocationRequest;
import com.foodordering.rider.dto.RiderRegistrationRequest;
import com.foodordering.security.JwtUtil;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/riders")
public class RiderController {

    private final RiderService riderService;
    private final JwtUtil jwtUtil;

    public RiderController(
            RiderService riderService,
            JwtUtil jwtUtil
    ) {
        this.riderService = riderService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerRider(
            @Valid
            @RequestBody
            RiderRegistrationRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        riderService.registerRider(
                                request
                        )
                );
    }

    @GetMapping("/me/dashboard")
    public ResponseEntity<RiderDashboardDto> getDashboard(
            @RequestHeader("Authorization")
            String authHeader
    ) {

        requireRider(authHeader);

        return ResponseEntity.ok(
                riderService.getDashboard(
                        extractUserId(authHeader)
                )
        );
    }

    @PatchMapping("/me/availability")
    public ResponseEntity<RiderDto> updateAvailability(
            @RequestHeader("Authorization")
            String authHeader,

            @Valid
            @RequestBody
            RiderAvailabilityRequest request
    ) {

        requireRider(authHeader);

        return ResponseEntity.ok(
                riderService.updateAvailability(
                        extractUserId(authHeader),
                        request
                )
        );
    }

    @PatchMapping("/me/location")
    public ResponseEntity<RiderDto> updateLocation(
            @RequestHeader("Authorization")
            String authHeader,

            @Valid
            @RequestBody
            RiderLocationRequest request
    ) {

        requireRider(authHeader);

        return ResponseEntity.ok(
                riderService.updateLocation(
                        extractUserId(authHeader),
                        request
                )
        );
    }

    @PatchMapping("/delivery-requests/{requestId}/accept")
    public ResponseEntity<DeliveryRequestDto> acceptRequest(
            @RequestHeader("Authorization")
            String authHeader,

            @PathVariable
            UUID requestId
    ) {

        requireRider(authHeader);

        return ResponseEntity.ok(
                riderService.acceptRequest(
                        extractUserId(authHeader),
                        requestId
                )
        );
    }

    @PatchMapping("/delivery-requests/{requestId}/reject")
    public ResponseEntity<DeliveryRequestDto> rejectRequest(
            @RequestHeader("Authorization")
            String authHeader,

            @PathVariable
            UUID requestId,

            @Valid
            @RequestBody
            RejectDeliveryRequest request
    ) {

        requireRider(authHeader);

        return ResponseEntity.ok(
                riderService.rejectRequest(
                        extractUserId(authHeader),
                        requestId,
                        request
                )
        );
    }

    @PostMapping("/delivery-requests/{requestId}/timeout")
    public ResponseEntity<DeliveryRequestDto> timeoutRequest(
            @RequestHeader("Authorization")
            String authHeader,

            @PathVariable
            UUID requestId
    ) {
        return ResponseEntity.ok(
                riderService.timeoutAndReassignRequest(requestId)
        );
    }

    @PatchMapping("/delivery-requests/{requestId}/arrived-restaurant")
    public ResponseEntity<DeliveryRequestDto> markArrivedAtRestaurant(
            @RequestHeader("Authorization")
            String authHeader,

            @PathVariable
            UUID requestId
    ) {

        requireRider(authHeader);

        return ResponseEntity.ok(
                riderService.markArrivedAtRestaurant(
                        extractUserId(authHeader),
                        requestId
                )
        );
    }

    @PatchMapping("/delivery-requests/{requestId}/pickup")
    public ResponseEntity<DeliveryRequestDto> confirmPickup(
            @RequestHeader("Authorization")
            String authHeader,

            @PathVariable
            UUID requestId
    ) {

        requireRider(authHeader);

        return ResponseEntity.ok(
                riderService.confirmPickup(
                        extractUserId(authHeader),
                        requestId
                )
        );
    }

    @PatchMapping("/delivery-requests/{requestId}/delivered")
    public ResponseEntity<DeliveryRequestDto> confirmDelivery(
            @RequestHeader("Authorization")
            String authHeader,

            @PathVariable
            UUID requestId
    ) {

        requireRider(authHeader);

        return ResponseEntity.ok(
                riderService.confirmDelivery(
                        extractUserId(authHeader),
                        requestId
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

    private void requireRider(
            String authHeader
    ) {

        String role =
                extractRole(authHeader);

        if (!"RIDER".equals(role)) {
            throw new ForbiddenOperationException(
                    "Only riders can access rider features"
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
}
