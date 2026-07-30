package com.foodordering.admin;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foodordering.User.dto.UserDto;
import com.foodordering.User.entity.Role;
import com.foodordering.User.repository.UserRepository;
import com.foodordering.restaurant.RestaurantDto;
import com.foodordering.restaurant.RestaurantRepository;
import com.foodordering.restaurant.RestaurantService;
import com.foodordering.restaurant.RestaurantStatus;
import com.foodordering.rider.DeliveryRequestRepository;
import com.foodordering.rider.Rider;
import com.foodordering.rider.RiderOperationalStatus;
import com.foodordering.rider.RiderRepository;
import com.foodordering.rider.RiderStatus;
import com.foodordering.rider.dto.DeliveryRequestDto;
import com.foodordering.rider.dto.RiderDto;
import com.foodordering.security.JwtUtil;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;
    private final RiderRepository riderRepository;
    private final DeliveryRequestRepository deliveryRequestRepository;
    private final JwtUtil jwtUtil;

    public AdminController(
            UserRepository userRepository,
            RestaurantRepository restaurantRepository,
            RestaurantService restaurantService,
            RiderRepository riderRepository,
            DeliveryRequestRepository deliveryRequestRepository,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.restaurantService = restaurantService;
        this.riderRepository = riderRepository;
        this.deliveryRequestRepository = deliveryRequestRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/customers")
    public ResponseEntity<?> getAllCustomers(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader
    ) {
        try {
            requireAdmin(authHeader);
            List<UserDto> customers = userRepository
                    .findByRole(Role.CUSTOMER)
                    .stream()
                    .map(UserDto::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(customers);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<?> deleteCustomer(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,
            @PathVariable UUID id
    ) {
        try {
            requireAdmin(authHeader);
            userRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @GetMapping("/owners")
    public ResponseEntity<?> getAllOwners(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader
    ) {
        try {
            requireAdmin(authHeader);
            List<UserDto> owners = userRepository
                    .findByRole(Role.OWNER)
                    .stream()
                    .map(UserDto::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(owners);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @GetMapping("/restaurants")
    public ResponseEntity<?> getAllRestaurants(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader
    ) {
        try {
            requireAdmin(authHeader);
            List<RestaurantDto> restaurants = restaurantRepository
                    .findAll()
                    .stream()
                    .map(RestaurantDto::new)
                    .toList();

            return ResponseEntity.ok(restaurants);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @GetMapping("/restaurants/pending")
    public ResponseEntity<?> getPendingRestaurants(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader
    ) {
        try {
            requireAdmin(authHeader);
            return ResponseEntity.ok(
                    restaurantService.getRestaurantsByStatus(
                            RestaurantStatus.PENDING_APPROVAL
                    )
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PatchMapping("/restaurants/{id}/status")
    public ResponseEntity<?> updateRestaurantStatus(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,
            @PathVariable UUID id,
            @RequestParam RestaurantStatus status
    ) {
        try {
            requireAdmin(authHeader);
            return ResponseEntity.ok(
                    restaurantService.updateApprovalStatus(
                            id,
                            status
                    )
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @DeleteMapping("/restaurants/{id}")
    public ResponseEntity<?> deleteRestaurant(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,
            @PathVariable UUID id
    ) {
        try {
            requireAdmin(authHeader);
            restaurantRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @GetMapping("/riders")
    public ResponseEntity<?> getAllRiders(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader
    ) {
        try {
            requireAdmin(authHeader);
            return ResponseEntity.ok(
                    riderRepository
                            .findAll()
                            .stream()
                            .map(RiderDto::new)
                            .toList()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PatchMapping("/riders/{id}/status")
    public ResponseEntity<?> updateRiderStatus(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,
            @PathVariable UUID id,
            @RequestParam RiderStatus status
    ) {
        try {
            requireAdmin(authHeader);

            Rider rider = riderRepository
                    .findById(id)
                    .orElseThrow(() ->
                            new RuntimeException("Rider not found")
                    );

            rider.setStatus(status);

            if (status != RiderStatus.APPROVED) {
                rider.setOnline(false);
                rider.setOperationalStatus(
                        RiderOperationalStatus.CLOSED
                );
            }

            return ResponseEntity.ok(
                    new RiderDto(
                            riderRepository.save(rider)
                    )
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @GetMapping("/riders/activities")
    public ResponseEntity<?> getRiderActivities(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader
    ) {
        try {
            requireAdmin(authHeader);
            return ResponseEntity.ok(
                    deliveryRequestRepository
                            .findAllByOrderByRequestedAtDesc()
                            .stream()
                            .map(DeliveryRequestDto::new)
                            .toList()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @GetMapping("/riders/{id}/activities")
    public ResponseEntity<?> getRiderActivities(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,
            @PathVariable UUID id
    ) {
        try {
            requireAdmin(authHeader);
            return ResponseEntity.ok(
                    deliveryRequestRepository
                            .findByRiderIdOrderByRequestedAtDesc(id)
                            .stream()
                            .map(DeliveryRequestDto::new)
                            .toList()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    private void requireAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization token is missing or invalid");
        }

        String token = authHeader.substring(7);
        String role = normalizeRole(jwtUtil.extractRole(token));

        if (!Role.SUPER_ADMIN.equals(role)) {
            throw new RuntimeException("Access denied: admin privileges required");
        }
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "";
        }

        String normalized = role.trim().toUpperCase(Locale.ROOT);

        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }

        return normalized;
    }
}
