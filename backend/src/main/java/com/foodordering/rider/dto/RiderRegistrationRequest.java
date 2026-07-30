package com.foodordering.rider.dto;

import com.foodordering.rider.VehicleType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RiderRegistrationRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must not exceed 150 characters")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(?:\\+254|254|0)?[\\s-]?[17](?:[\\s-]?\\d){8}$",
            message = "Enter a valid Kenyan phone number"
    )
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must contain between 8 and 72 characters")
    private String password;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @NotBlank(message = "Licence plate is required")
    @Size(max = 50, message = "Licence plate must not exceed 50 characters")
    private String licencePlate;

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

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getLicencePlate() {
        return licencePlate;
    }

    public void setLicencePlate(String licencePlate) {
        this.licencePlate = licencePlate;
    }
}
