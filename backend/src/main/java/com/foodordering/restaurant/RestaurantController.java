package com.foodordering.restaurant;

import com.foodordering.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final JwtUtil jwtUtil;

    public RestaurantController(RestaurantService restaurantService, JwtUtil jwtUtil) {
        this.restaurantService = restaurantService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<?> createRestaurant(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody RestaurantDto dto) {
        try {
            UUID ownerId = extractUserId(authHeader);
            RestaurantDto created = restaurantService.createRestaurant(ownerId, dto);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyRestaurant(@RequestHeader("Authorization") String authHeader) {
        try {
            UUID ownerId = extractUserId(authHeader);
            RestaurantDto dto = restaurantService.getMyRestaurant(ownerId);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    private UUID extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extractUserId(token);
    }
}