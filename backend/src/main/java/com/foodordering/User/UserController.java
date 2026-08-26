package com.foodordering.User;

import com.foodordering.User.dto.*;
import com.foodordering.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @RequestHeader("Authorization") String authHeader
    ) {
        UUID userId = extractUserId(authHeader);
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UUID userId = extractUserId(authHeader);
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        UUID userId = extractUserId(authHeader);
        userService.changePassword(userId, request);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<SavedAddressDto>> getSavedAddresses(
            @RequestHeader("Authorization") String authHeader
    ) {
        UUID userId = extractUserId(authHeader);
        return ResponseEntity.ok(userService.getSavedAddresses(userId));
    }

    @PostMapping("/addresses")
    public ResponseEntity<SavedAddressDto> createSavedAddress(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody SavedAddressRequest request
    ) {
        UUID userId = extractUserId(authHeader);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createSavedAddress(userId, request));
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<SavedAddressDto> updateSavedAddress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID addressId,
            @Valid @RequestBody SavedAddressRequest request
    ) {
        UUID userId = extractUserId(authHeader);
        return ResponseEntity.ok(userService.updateSavedAddress(userId, addressId, request));
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Map<String, String>> deleteSavedAddress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID addressId
    ) {
        UUID userId = extractUserId(authHeader);
        userService.deleteSavedAddress(userId, addressId);
        return ResponseEntity.ok(Map.of("message", "Address deleted successfully"));
    }

    @PutMapping("/addresses/{addressId}/default")
    public ResponseEntity<SavedAddressDto> setDefaultAddress(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID addressId
    ) {
        UUID userId = extractUserId(authHeader);
        return ResponseEntity.ok(userService.setDefaultAddress(userId, addressId));
    }

    private UUID extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        return jwtUtil.extractUserId(token);
    }
}
