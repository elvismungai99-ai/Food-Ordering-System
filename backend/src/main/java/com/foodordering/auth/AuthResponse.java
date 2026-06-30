package com.foodordering.auth;

import java.util.UUID;

public class AuthResponse {

    private String token;
    private UUID userId;
    private String role;
    private String firstName;

    public AuthResponse(String token, UUID userId, String role, String firstName) {
        this.token = token;
        this.userId = userId;
        this.role = role;
        this.firstName = firstName;
    }

    public String getToken() {
        return token;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public String getFirstName() {
        return firstName;
    }
}