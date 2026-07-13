package com.foodordering.restaurant;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foodordering.security.JwtUtil;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final JwtUtil jwtUtil;

    public RestaurantController(
            RestaurantService restaurantService,
            JwtUtil jwtUtil
    ) {
        this.restaurantService = restaurantService;
        this.jwtUtil = jwtUtil;
    }

    // Public — get all restaurants, with optional search and category filtering.
    @GetMapping
    public ResponseEntity<List<RestaurantDto>> getAllRestaurants(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category
    ) {
        List<RestaurantDto> restaurants;

        boolean hasSearch =
                search != null && !search.isBlank();

        boolean hasCategory =
                category != null && !category.isBlank();

        if (hasSearch || hasCategory) {
            restaurants = restaurantService.searchRestaurants(
                    search,
                    category
            );
        } else {
            restaurants =
                    restaurantService.getAllRestaurants();
        }

        return ResponseEntity.ok(restaurants);
    }

    // Public — get all available restaurant categories.
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories =
                restaurantService.getAllCategories();

        return ResponseEntity.ok(categories);
    }

    // Restaurant owner — get the restaurant owned by the logged-in account.
    @GetMapping("/me")
    public ResponseEntity<?> getMyRestaurant(
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            UUID ownerId = extractUserId(authHeader);

            RestaurantDto restaurant =
                    restaurantService.getMyRestaurant(ownerId);

            return ResponseEntity.ok(restaurant);

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }

    // Public — get one restaurant by its ID.
    @GetMapping("/{id}")
    public ResponseEntity<?> getRestaurantById(
            @PathVariable UUID id
    ) {
        try {
            RestaurantDto restaurant =
                    restaurantService.getRestaurantById(id);

            return ResponseEntity.ok(restaurant);

        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(exception.getMessage());
        }
    }

    // Restaurant owner — create a restaurant for the logged-in account.
    @PostMapping
    public ResponseEntity<?> createRestaurant(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody RestaurantDto dto
    ) {
        try {
            UUID ownerId = extractUserId(authHeader);

            RestaurantDto createdRestaurant =
                    restaurantService.createRestaurant(
                            ownerId,
                            dto
                    );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createdRestaurant);

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }

    // Restaurant owner — update their own restaurant.
    @PutMapping("/me")
    public ResponseEntity<?> updateMyRestaurant(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody RestaurantDto dto
    ) {
        try {
            UUID ownerId = extractUserId(authHeader);

            RestaurantDto updatedRestaurant =
                    restaurantService.updateMyRestaurant(
                            ownerId,
                            dto
                    );

            return ResponseEntity.ok(updatedRestaurant);

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }

    private UUID extractUserId(String authHeader) {
        if (
                authHeader == null
                || !authHeader.startsWith("Bearer ")
        ) {
            throw new RuntimeException(
                    "Authorization token is missing or invalid"
            );
        }

        String token =
                authHeader.substring(7);

        return jwtUtil.extractUserId(token);
    }
}
