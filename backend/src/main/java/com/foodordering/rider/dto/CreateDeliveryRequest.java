package com.foodordering.rider.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateDeliveryRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Rider ID is required")
    private UUID riderId;

    private BigDecimal restaurantLatitude;

    private BigDecimal restaurantLongitude;

    @DecimalMin(value = "0.00", message = "Estimated payout cannot be negative")
    private BigDecimal estimatedPayout;

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getRiderId() {
        return riderId;
    }

    public void setRiderId(UUID riderId) {
        this.riderId = riderId;
    }

    public BigDecimal getRestaurantLatitude() {
        return restaurantLatitude;
    }

    public void setRestaurantLatitude(BigDecimal restaurantLatitude) {
        this.restaurantLatitude = restaurantLatitude;
    }

    public BigDecimal getRestaurantLongitude() {
        return restaurantLongitude;
    }

    public void setRestaurantLongitude(BigDecimal restaurantLongitude) {
        this.restaurantLongitude = restaurantLongitude;
    }

    public BigDecimal getEstimatedPayout() {
        return estimatedPayout;
    }

    public void setEstimatedPayout(BigDecimal estimatedPayout) {
        this.estimatedPayout = estimatedPayout;
    }
}
