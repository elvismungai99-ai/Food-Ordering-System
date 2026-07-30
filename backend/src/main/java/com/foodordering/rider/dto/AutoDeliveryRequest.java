package com.foodordering.rider.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class AutoDeliveryRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Restaurant latitude is required")
    private BigDecimal restaurantLatitude;

    @NotNull(message = "Restaurant longitude is required")
    private BigDecimal restaurantLongitude;

    @DecimalMin(value = "0.00", message = "Estimated payout cannot be negative")
    private BigDecimal estimatedPayout;

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
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
