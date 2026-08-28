package com.foodordering.rider;

import com.foodordering.User.entity.User;
import com.foodordering.rider.dto.AutoDeliveryRequest;
import com.foodordering.rider.dto.CreateDeliveryRequest;
import com.foodordering.rider.dto.DeliveryRequestDto;
import com.foodordering.rider.dto.RiderDto;
import com.foodordering.security.SecurityUtils;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-dispatch")
public class DeliveryDispatchController {

    private final RiderService riderService;
    private final SecurityUtils securityUtils;

    public DeliveryDispatchController(
            RiderService riderService,
            SecurityUtils securityUtils
    ) {
        this.riderService = riderService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/available-riders")
    public ResponseEntity<List<RiderDto>> getAvailableRiders() {
        securityUtils.requireOwner();
        return ResponseEntity.ok(
                riderService.getAvailableRiders()
        );
    }

    @GetMapping("/requests")
    public ResponseEntity<List<DeliveryRequestDto>> getRestaurantRequests() {
        User owner = securityUtils.requireOwner();
        return ResponseEntity.ok(
                riderService.getRestaurantDeliveryRequests(owner.getId())
        );
    }

    @PostMapping("/requests")
    public ResponseEntity<DeliveryRequestDto> createDeliveryRequest(
            @Valid @RequestBody CreateDeliveryRequest request
    ) {
        User owner = securityUtils.requireOwner();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        riderService.createDeliveryRequest(
                                owner.getId(),
                                request
                        )
                );
    }

    @PostMapping("/requests/auto")
    public ResponseEntity<DeliveryRequestDto> createAutomaticDeliveryRequest(
            @Valid @RequestBody AutoDeliveryRequest request
    ) {
        User owner = securityUtils.requireOwner();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        riderService.createAutomaticDeliveryRequest(
                                owner.getId(),
                                request
                        )
                );
    }
}
