package com.foodordering.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(
            message = "Full name is required"
    )
    @Size(
            max = 150,
            message = "Full name must not exceed 150 characters"
    )
    private String fullName;

    @NotBlank(
            message = "Email is required"
    )
    @Email(
            message = "Enter a valid email address"
    )
    private String email;

    @NotBlank(
            message = "Password is required"
    )
    @Size(
            min = 8,
            max = 72,
            message = "Password must contain between 8 and 72 characters"
    )
    private String password;

    @NotBlank(
            message = "Phone number is required"
    )
    @Pattern(
            regexp = "^(?:\\+254|254|0)?[\\s-]?[17](?:[\\s-]?\\d){8}$",
            message = "Enter a valid Kenyan phone number, for example 0712345678 or +254712345678"
    )
    private String phoneNumber;

    @NotNull(
            message = "Role is required"
    )
    private String role;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
