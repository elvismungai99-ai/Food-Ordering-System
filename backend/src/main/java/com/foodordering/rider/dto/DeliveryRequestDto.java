package com.foodordering.rider.dto;

import com.foodordering.rider.DeliveryRequest;
import com.foodordering.rider.DeliveryRequestStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class DeliveryRequestDto {

    private UUID id;
    private UUID orderId;
    private UUID riderId;
    private UUID restaurantId;
    private String restaurantName;
    private String restaurantAddress;
    private BigDecimal restaurantLatitude;
    private BigDecimal restaurantLongitude;
    private String customerAddress;
    private BigDecimal customerLatitude;
    private BigDecimal customerLongitude;
    private BigDecimal distanceKm;
    private BigDecimal estimatedPayout;
    private BigDecimal assignmentScore;
    private DeliveryRequestStatus status;
    private String rejectionReason;
    private String requestedAt;
    private String respondedAt;
    private String arrivedAtRestaurantAt;
    private String pickedUpAt;
    private String deliveredAt;

    public DeliveryRequestDto() {
    }

    public DeliveryRequestDto(DeliveryRequest request) {
        id = request.getId();
        orderId = request.getOrderId();
        riderId = request.getRiderId();
        restaurantId = request.getRestaurantId();
        restaurantName = request.getRestaurantName();
        restaurantAddress = request.getRestaurantAddress();
        restaurantLatitude = request.getRestaurantLatitude();
        restaurantLongitude = request.getRestaurantLongitude();
        customerAddress = request.getCustomerAddress();
        customerLatitude = request.getCustomerLatitude();
        customerLongitude = request.getCustomerLongitude();
        distanceKm = request.getDistanceKm();
        estimatedPayout = request.getEstimatedPayout();
        assignmentScore = request.getAssignmentScore();
        status = request.getStatus();
        rejectionReason = request.getRejectionReason();
        requestedAt = request.getRequestedAt() != null
                ? request.getRequestedAt().toString()
                : null;
        respondedAt = request.getRespondedAt() != null
                ? request.getRespondedAt().toString()
                : null;
        arrivedAtRestaurantAt = request.getArrivedAtRestaurantAt() != null
                ? request.getArrivedAtRestaurantAt().toString()
                : null;
        pickedUpAt = request.getPickedUpAt() != null
                ? request.getPickedUpAt().toString()
                : null;
        deliveredAt = request.getDeliveredAt() != null
                ? request.getDeliveredAt().toString()
                : null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getRiderId() {
        return riderId;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getRestaurantAddress() {
        return restaurantAddress;
    }

    public BigDecimal getRestaurantLatitude() {
        return restaurantLatitude;
    }

    public BigDecimal getRestaurantLongitude() {
        return restaurantLongitude;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public BigDecimal getCustomerLatitude() {
        return customerLatitude;
    }

    public BigDecimal getCustomerLongitude() {
        return customerLongitude;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public BigDecimal getEstimatedPayout() {
        return estimatedPayout;
    }

    public BigDecimal getAssignmentScore() {
        return assignmentScore;
    }

    public DeliveryRequestStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public String getRequestedAt() {
        return requestedAt;
    }

    public String getRespondedAt() {
        return respondedAt;
    }

    public String getArrivedAtRestaurantAt() {
        return arrivedAtRestaurantAt;
    }

    public String getPickedUpAt() {
        return pickedUpAt;
    }

    public String getDeliveredAt() {
        return deliveredAt;
    }
}
