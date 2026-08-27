package com.foodordering.auth;

import java.util.UUID;

public class AuthResponse {

    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn = 900;
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
        this.tokenType = "Bearer";
        this.expiresIn = 900;
    }

    public AuthResponse(String token, String refreshToken, UUID userId, String role, String firstName, long expiresIn) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.role = role;
        this.firstName = firstName;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
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