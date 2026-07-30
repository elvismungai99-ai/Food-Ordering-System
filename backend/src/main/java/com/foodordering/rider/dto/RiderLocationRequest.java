package com.foodordering.rider.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class RiderLocationRequest {

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude cannot be less than -90")
    @DecimalMax(value = "90.0", message = "Latitude cannot be greater than 90")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude cannot be less than -180")
    @DecimalMax(value = "180.0", message = "Longitude cannot be greater than 180")
    private BigDecimal longitude;

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
}
