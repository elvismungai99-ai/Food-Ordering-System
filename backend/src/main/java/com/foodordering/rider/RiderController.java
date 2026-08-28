package com.foodordering.rider;

import com.foodordering.User.entity.User;
import com.foodordering.auth.AuthResponse;
import com.foodordering.rider.dto.DeliveryRequestDto;
import com.foodordering.rider.dto.RejectDeliveryRequest;
import com.foodordering.rider.dto.RiderAvailabilityRequest;
import com.foodordering.rider.dto.RiderDashboardDto;
import com.foodordering.rider.dto.RiderDto;
import com.foodordering.rider.dto.RiderLocationRequest;
import com.foodordering.rider.dto.RiderRegistrationRequest;
import com.foodordering.security.SecurityUtils;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/riders")
public class RiderController {

    private final RiderService riderService;
    private final SecurityUtils securityUtils;

    public RiderController(
            RiderService riderService,
            SecurityUtils securityUtils
    ) {
        this.riderService = riderService;
        this.securityUtils = securityUtils;
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
    public ResponseEntity<RiderDashboardDto> getDashboard() {
        User rider = requireRider();
        return ResponseEntity.ok(
                riderService.getDashboard(rider.getId())
        );
    }

    @PatchMapping("/me/availability")
    public ResponseEntity<RiderDto> updateAvailability(
            @Valid
            @RequestBody
            RiderAvailabilityRequest request
    ) {
        User rider = requireRider();
        return ResponseEntity.ok(
                riderService.updateAvailability(
                        rider.getId(),
                        request
                )
        );
    }

    @PatchMapping("/me/location")
    public ResponseEntity<RiderDto> updateLocation(
            @Valid
            @RequestBody
            RiderLocationRequest request
    ) {
        User rider = requireRider();
        return ResponseEntity.ok(
                riderService.updateLocation(
                        rider.getId(),
                        request
                )
        );
    }

    @PatchMapping("/delivery-requests/{requestId}/accept")
    public ResponseEntity<DeliveryRequestDto> acceptRequest(
            @PathVariable UUID requestId
    ) {
        User rider = requireRider();
        return ResponseEntity.ok(
                riderService.acceptRequest(
                        rider.getId(),
                        requestId
                )
        );
    }

    @PatchMapping("/delivery-requests/{requestId}/reject")
    public ResponseEntity<DeliveryRequestDto> rejectRequest(
            @PathVariable UUID requestId,
            @Valid
            @RequestBody
            RejectDeliveryRequest request
    ) {
        User rider = requireRider();
        return ResponseEntity.ok(
                riderService.rejectRequest(
                        rider.getId(),
                        requestId,
                        request
                )
        );
    }

    @PostMapping("/delivery-requests/{requestId}/timeout")
    public ResponseEntity<DeliveryRequestDto> timeoutRequest(
            @PathVariable UUID requestId
    ) {
        return ResponseEntity.ok(
                riderService.timeoutAndReassignRequest(requestId)
        );
    }

    @PatchMapping("/delivery-requests/{requestId}/arrived-restaurant")
    public ResponseEntity<DeliveryRequestDto> markArrivedAtRestaurant(
            @PathVariable UUID requestId
    ) {
        User rider = requireRider();
        return ResponseEntity.ok(
                riderService.markArrivedAtRestaurant(
                        rider.getId(),
                        requestId
                )
        );
    }

    @PatchMapping("/delivery-requests/{requestId}/pickup")
    public ResponseEntity<DeliveryRequestDto> confirmPickup(
            @PathVariable UUID requestId
    ) {
        User rider = requireRider();
        return ResponseEntity.ok(
                riderService.confirmPickup(
                        rider.getId(),
                        requestId
                )
        );
    }

    @PatchMapping("/delivery-requests/{requestId}/delivered")
    public ResponseEntity<DeliveryRequestDto> confirmDelivery(
            @PathVariable UUID requestId
    ) {
        User rider = requireRider();
        return ResponseEntity.ok(
                riderService.confirmDelivery(
                        rider.getId(),
                        requestId
                )
        );
    }

    private User requireRider() {
        securityUtils.requireRoles("RIDER");
        return securityUtils.getCurrentUser();
    }
}
