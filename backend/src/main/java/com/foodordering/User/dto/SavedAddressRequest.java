package com.foodordering.User.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class SavedAddressRequest {

    @NotBlank(message = "Label is required (e.g. Home, Work)")
    @Size(max = 50, message = "Label is too long")
    private String label;

    @NotBlank(message = "Address is required")
    private String address;

    @Size(max = 150, message = "Building name is too long")
    private String buildingName;

    @Size(max = 100, message = "Apartment number is too long")
    private String apartmentNumber;

    private String landmarks;

    private String deliveryInstructions;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Boolean isDefault;

    public SavedAddressRequest() {
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

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}

