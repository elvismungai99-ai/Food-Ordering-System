package com.foodordering.security;

import java.util.Locale;

import com.foodordering.User.entity.User;
import com.foodordering.User.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(
            UserRepository userRepository
    ) {
        this.userRepository =
                userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(
            String email
    ) throws UsernameNotFoundException {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "User not found"
                                )
                        );

        String normalizedRole = normalizeRole(user.getRole());

        return org.springframework.security
                .core
                .userdetails
                .User
                .withUsername(
                        user.getEmail()
                )
                .password(
                        user.getPasswordHash()
                )
                .roles(
                        normalizedRole
                )
                .disabled(
                        !user.isActive()
                )
                .build();
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }

        String normalized = role.trim().toUpperCase(Locale.ROOT);

        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }

        if (
                normalized.equals("RESTAURANT_ADMIN")
                || normalized.equals("RESTAURANT_OWNER")
                || normalized.equals("ADMIN_RESTAURANT")
        ) {
            return "OWNER";
        }

        return normalized;
    }
}
