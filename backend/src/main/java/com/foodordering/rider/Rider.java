package com.foodordering.rider;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "riders")
public class Rider {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @Column(name = "licence_plate", nullable = false)
    private String licencePlate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "operational_status", nullable = false)
    private RiderOperationalStatus operationalStatus;

    @Column(name = "online", nullable = false)
    private boolean online;

    @Column(name = "total_rejections", nullable = false)
    private int totalRejections;

    @Column(name = "current_latitude", precision = 9, scale = 6)
    private BigDecimal currentLatitude;

    @Column(name = "current_longitude", precision = 10, scale = 6)
    private BigDecimal currentLongitude;

    @Column(name = "last_location_updated_at")
    private LocalDateTime lastLocationUpdatedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = RiderStatus.PENDING_APPROVAL;
        }

        if (operationalStatus == null) {
            operationalStatus = RiderOperationalStatus.CLOSED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public RiderStatus getStatus() {
        return status;
    }

    public void setStatus(RiderStatus status) {
        this.status = status;
    }

    public RiderOperationalStatus getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(RiderOperationalStatus operationalStatus) {
        this.operationalStatus = operationalStatus;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int getTotalRejections() {
        return totalRejections;
    }

    public void setTotalRejections(int totalRejections) {
        this.totalRejections = totalRejections;
    }

    public void incrementRejections() {
        totalRejections++;
    }

    public BigDecimal getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(BigDecimal currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public BigDecimal getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(BigDecimal currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public LocalDateTime getLastLocationUpdatedAt() {
        return lastLocationUpdatedAt;
    }

    public void setLastLocationUpdatedAt(LocalDateTime lastLocationUpdatedAt) {
        this.lastLocationUpdatedAt = lastLocationUpdatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
