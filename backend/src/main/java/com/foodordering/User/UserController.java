package com.foodordering.User;

import com.foodordering.User.dto.*;
import com.foodordering.User.entity.User;
import com.foodordering.security.SecurityUtils;
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
    private final SecurityUtils securityUtils;

    public UserController(UserService userService, SecurityUtils securityUtils) {
        this.userService = userService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile() {
        User user = securityUtils.getCurrentUser();
        return ResponseEntity.ok(userService.getProfile(user.getId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        User user = securityUtils.getCurrentUser();
        return ResponseEntity.ok(userService.updateProfile(user.getId(), request));
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        User user = securityUtils.getCurrentUser();
        userService.changePassword(user.getId(), request);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<SavedAddressDto>> getSavedAddresses() {
        User user = securityUtils.requireCustomer();
        return ResponseEntity.ok(userService.getSavedAddresses(user.getId()));
    }

    @PostMapping("/addresses")
    public ResponseEntity<SavedAddressDto> createSavedAddress(
            @Valid @RequestBody SavedAddressRequest request
    ) {
        User user = securityUtils.requireCustomer();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createSavedAddress(user.getId(), request));
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<SavedAddressDto> updateSavedAddress(
            @PathVariable UUID addressId,
            @Valid @RequestBody SavedAddressRequest request
    ) {
        User user = securityUtils.requireCustomer();
        return ResponseEntity.ok(userService.updateSavedAddress(user.getId(), addressId, request));
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Map<String, String>> deleteSavedAddress(
            @PathVariable UUID addressId
    ) {
        User user = securityUtils.requireCustomer();
        userService.deleteSavedAddress(user.getId(), addressId);
        return ResponseEntity.ok(Map.of("message", "Address deleted successfully"));
    }

    @PutMapping("/addresses/{addressId}/default")
    public ResponseEntity<SavedAddressDto> setDefaultAddress(
            @PathVariable UUID addressId
    ) {
        User user = securityUtils.requireCustomer();
        return ResponseEntity.ok(userService.setDefaultAddress(user.getId(), addressId));
    }
}
