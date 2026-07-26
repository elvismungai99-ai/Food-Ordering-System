package com.foodordering.auth;

import java.util.UUID;

public class AuthResponse {

    private String token;
    private UUID userId;
    private String role;
    private String firstName;

    public AuthResponse() {
    }

    public AuthResponse(String token, UUID userId, String role, String firstName) {
        this.token = token;
        this.userId = userId;
        this.role = role;
        this.firstName = firstName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}