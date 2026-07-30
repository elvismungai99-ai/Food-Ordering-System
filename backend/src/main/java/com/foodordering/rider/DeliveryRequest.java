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
@Table(name = "delivery_requests")
public class DeliveryRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "rider_id", nullable = false)
    private UUID riderId;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "restaurant_name", nullable = false)
    private String restaurantName;

    @Column(name = "restaurant_address", nullable = false, columnDefinition = "TEXT")
    private String restaurantAddress;

    @Column(name = "restaurant_latitude", precision = 9, scale = 6)
    private BigDecimal restaurantLatitude;

    @Column(name = "restaurant_longitude", precision = 10, scale = 6)
    private BigDecimal restaurantLongitude;

    @Column(name = "customer_address", nullable = false, columnDefinition = "TEXT")
    private String customerAddress;

    @Column(name = "customer_latitude", precision = 9, scale = 6)
    private BigDecimal customerLatitude;

    @Column(name = "customer_longitude", precision = 10, scale = 6)
    private BigDecimal customerLongitude;

    @Column(name = "distance_km", precision = 8, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "estimated_payout", nullable = false, precision = 12, scale = 2)
    private BigDecimal estimatedPayout;

    @Column(name = "assignment_score", precision = 10, scale = 2)
    private BigDecimal assignmentScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryRequestStatus status;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "arrived_at_restaurant_at")
    private LocalDateTime arrivedAtRestaurantAt;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        requestedAt = now;
        updatedAt = now;

        if (status == null) {
            status = DeliveryRequestStatus.REQUESTED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

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

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(UUID restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantAddress() {
        return restaurantAddress;
    }

    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
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

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public BigDecimal getCustomerLatitude() {
        return customerLatitude;
    }

    public void setCustomerLatitude(BigDecimal customerLatitude) {
        this.customerLatitude = customerLatitude;
    }

    public BigDecimal getCustomerLongitude() {
        return customerLongitude;
    }

    public void setCustomerLongitude(BigDecimal customerLongitude) {
        this.customerLongitude = customerLongitude;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(BigDecimal distanceKm) {
        this.distanceKm = distanceKm;
    }

    public BigDecimal getEstimatedPayout() {
        return estimatedPayout;
    }

    public void setEstimatedPayout(BigDecimal estimatedPayout) {
        this.estimatedPayout = estimatedPayout;
    }

    public BigDecimal getAssignmentScore() {
        return assignmentScore;
    }

    public void setAssignmentScore(BigDecimal assignmentScore) {
        this.assignmentScore = assignmentScore;
    }

    public DeliveryRequestStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryRequestStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public LocalDateTime getArrivedAtRestaurantAt() {
        return arrivedAtRestaurantAt;
    }

    public void setArrivedAtRestaurantAt(LocalDateTime arrivedAtRestaurantAt) {
        this.arrivedAtRestaurantAt = arrivedAtRestaurantAt;
    }

    public LocalDateTime getPickedUpAt() {
        return pickedUpAt;
    }

    public void setPickedUpAt(LocalDateTime pickedUpAt) {
        this.pickedUpAt = pickedUpAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
