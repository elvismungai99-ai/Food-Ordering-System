package com.foodordering.security;

import com.foodordering.User.entity.User;
import com.foodordering.User.repository.UserRepository;
import com.foodordering.common.exception.ForbiddenOperationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Always retrieves the fresh User entity from the database.
     * Never trusts old claims in unverified JWT headers.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ForbiddenOperationException("User is not authenticated");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ForbiddenOperationException("Authenticated user no longer exists in database"));

        if (!user.isActive()) {
            throw new ForbiddenOperationException("This account is currently disabled");
        }

        return user;
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public String getCurrentUserRole() {
        return normalizeRole(getCurrentUser().getRole());
    }

    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(getCurrentUserRole());
    }

    public User requireCustomer() {
        User user = getCurrentUser();
        String role = normalizeRole(user.getRole());
        if (!"CUSTOMER".equals(role)) {
            throw new ForbiddenOperationException("Only customer accounts can perform this action");
        }
        return user;
    }

    public User requireOwner() {
        User user = getCurrentUser();
        String role = normalizeRole(user.getRole());
        if (!"OWNER".equals(role) && !"SUPER_ADMIN".equals(role)) {
            throw new ForbiddenOperationException("Only restaurant owners can perform this action");
        }
        return user;
    }

    public User requireSuperAdmin() {
        User user = getCurrentUser();
        if (!"SUPER_ADMIN".equals(normalizeRole(user.getRole()))) {
            throw new ForbiddenOperationException("Only super admins can perform this action");
        }
        return user;
    }

    public void requireOwnerOrSuperAdmin(UUID restaurantOwnerId) {
        User user = getCurrentUser();
        String role = normalizeRole(user.getRole());

        if ("SUPER_ADMIN".equals(role)) {
            return;
        }

        if (!"OWNER".equals(role) || restaurantOwnerId == null || !user.getId().equals(restaurantOwnerId)) {
            throw new ForbiddenOperationException("You do not have permission to manage this restaurant");
        }
    }

    public void requireSameUserOrSuperAdmin(UUID targetUserId) {
        User user = getCurrentUser();
        String role = normalizeRole(user.getRole());

        if ("SUPER_ADMIN".equals(role)) {
            return;
        }

        if (targetUserId == null || !user.getId().equals(targetUserId)) {
            throw new ForbiddenOperationException("You are not authorized to view or modify this user's data");
        }
    }

    public void requireRoles(String... allowedRoles) {
        String currentRole = getCurrentUserRole();
        boolean match = Arrays.stream(allowedRoles)
                .map(this::normalizeRole)
                .anyMatch(r -> r.equals(currentRole));

        if (!match) {
            throw new ForbiddenOperationException("Access denied for role: " + currentRole);
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
        if (normalized.equals("RESTAURANT_ADMIN") || normalized.equals("RESTAURANT_OWNER") || normalized.equals("ADMIN_RESTAURANT")) {
            return "OWNER";
        }
        return normalized;
    }
}

