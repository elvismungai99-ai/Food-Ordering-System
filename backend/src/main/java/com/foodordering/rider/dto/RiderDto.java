package com.foodordering.rider.dto;

import com.foodordering.rider.Rider;
import com.foodordering.rider.RiderOperationalStatus;
import com.foodordering.rider.RiderStatus;
import com.foodordering.rider.VehicleType;

import java.util.UUID;
import java.math.BigDecimal;

public class RiderDto {

    private UUID id;
    private UUID userId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private VehicleType vehicleType;
    private String licencePlate;
    private RiderStatus status;
    private RiderOperationalStatus operationalStatus;
    private boolean online;
    private int totalRejections;
    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
    private String lastLocationUpdatedAt;
    private String createdAt;
    private String updatedAt;

    public RiderDto() {
    }

    public RiderDto(Rider rider) {
        id = rider.getId();
        userId = rider.getUserId();
        fullName = rider.getFullName();
        phoneNumber = rider.getPhoneNumber();
        email = rider.getEmail();
        vehicleType = rider.getVehicleType();
        licencePlate = rider.getLicencePlate();
        status = rider.getStatus();
        operationalStatus = rider.getOperationalStatus();
        online = rider.isOnline();
        totalRejections = rider.getTotalRejections();
        currentLatitude = rider.getCurrentLatitude();
        currentLongitude = rider.getCurrentLongitude();
        lastLocationUpdatedAt =
                rider.getLastLocationUpdatedAt() != null
                        ? rider.getLastLocationUpdatedAt().toString()
                        : null;
        createdAt = rider.getCreatedAt() != null
                ? rider.getCreatedAt().toString()
                : null;
        updatedAt = rider.getUpdatedAt() != null
                ? rider.getUpdatedAt().toString()
                : null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public String getLicencePlate() {
        return licencePlate;
    }

    public RiderStatus getStatus() {
        return status;
    }

    public RiderOperationalStatus getOperationalStatus() {
        return operationalStatus;
    }

    public boolean isOnline() {
        return online;
    }

    public int getTotalRejections() {
        return totalRejections;
    }

    public BigDecimal getCurrentLatitude() {
        return currentLatitude;
    }

    public BigDecimal getCurrentLongitude() {
        return currentLongitude;
    }

    public String getLastLocationUpdatedAt() {
        return lastLocationUpdatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
