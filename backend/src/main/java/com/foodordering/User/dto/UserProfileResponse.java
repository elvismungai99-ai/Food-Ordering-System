package com.foodordering.User.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserProfileResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String role;
    private LocalDateTime createdAt;

    public UserProfileResponse() {
    }

    public UserProfileResponse(UUID id, String email, String firstName, String lastName, String fullName, String phoneNumber, String role, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

