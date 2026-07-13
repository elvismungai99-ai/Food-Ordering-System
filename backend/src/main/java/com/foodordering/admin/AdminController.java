package com.foodordering.admin;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodordering.User.dto.UserDto;
import com.foodordering.User.entity.Role;
import com.foodordering.User.repository.UserRepository;
import com.foodordering.restaurant.RestaurantRepository;
import com.foodordering.security.JwtUtil;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final JwtUtil jwtUtil;

    public AdminController(UserRepository userRepository,
                            RestaurantRepository restaurantRepository,
                            JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/customers")
    public ResponseEntity<?> getAllCustomers(@RequestHeader("Authorization") String authHeader) {
        try {
            requireAdmin(authHeader);
            List<UserDto> customers = userRepository.findByRole(Role.CUSTOMER)
                    .stream()
                    .map(UserDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(customers);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<?> deleteCustomer(@RequestHeader("Authorization") String authHeader,
                                             @PathVariable UUID id) {
        try {
            requireAdmin(authHeader);
            userRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @GetMapping("/owners")
    public ResponseEntity<?> getAllOwners(@RequestHeader("Authorization") String authHeader) {
        try {
            requireAdmin(authHeader);
            List<UserDto> owners = userRepository.findByRole(Role.OWNER)
                    .stream()
                    .map(UserDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(owners);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @GetMapping("/restaurants")
    public ResponseEntity<?> getAllRestaurants(@RequestHeader("Authorization") String authHeader) {
        try {
            requireAdmin(authHeader);
            return ResponseEntity.ok(restaurantRepository.findAll());
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @DeleteMapping("/restaurants/{id}")
    public ResponseEntity<?> deleteRestaurant(@RequestHeader("Authorization") String authHeader,
                                               @PathVariable UUID id) {
        try {
            requireAdmin(authHeader);
            restaurantRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    private void requireAdmin(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String role = jwtUtil.extractRole(token);
        if (!Role.SUPER_ADMIN.equals(role)) {
            throw new RuntimeException("Access denied: admin privileges required");
        }
    }
}