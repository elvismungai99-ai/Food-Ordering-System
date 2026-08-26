package com.foodordering.User.dto;

import com.foodordering.User.entity.SavedAddress;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class SavedAddressDto {
    private UUID id;
    private UUID userId;
    private String label;
    private String address;
    private String buildingName;
    private String apartmentNumber;
    private String landmarks;
    private String deliveryInstructions;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private boolean isDefault;
    private LocalDateTime createdAt;

    public SavedAddressDto() {
    }

    public SavedAddressDto(SavedAddress entity) {
        this.id = entity.getId();
        this.userId = entity.getUserId();
        this.label = entity.getLabel();
        this.address = entity.getAddress();
        this.buildingName = entity.getBuildingName();
        this.apartmentNumber = entity.getApartmentNumber();
        this.landmarks = entity.getLandmarks();
        this.deliveryInstructions = entity.getDeliveryInstructions();
        this.latitude = entity.getLatitude();
        this.longitude = entity.getLongitude();
        this.isDefault = entity.isDefault();
        this.createdAt = entity.getCreatedAt();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getApartmentNumber() {
        return apartmentNumber;
    }

    public void setApartmentNumber(String apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
    }

    public String getLandmarks() {
        return landmarks;
    }

    public void setLandmarks(String landmarks) {
        this.landmarks = landmarks;
    }

    public String getDeliveryInstructions() {
        return deliveryInstructions;
    }

    public void setDeliveryInstructions(String deliveryInstructions) {
        this.deliveryInstructions = deliveryInstructions;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

