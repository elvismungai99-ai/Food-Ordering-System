package com.foodordering.User.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.foodordering.User.entity.User;

public class UserDto {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String role;
    private boolean active;
    private LocalDateTime createdAt;

    public UserDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.fullName = user.getFullName();
        this.role = user.getRole();
        this.active = user.isActive();
        this.createdAt = user.getCreatedAt();
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}