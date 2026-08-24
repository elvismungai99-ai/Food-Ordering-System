package com.foodordering.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/*
 * Payload for GET /api/orders/{orderId}/tracking.
 *
 * Contains everything the customer needs to
 * follow their delivery on a live map:
 * - restaurant pickup coordinates
 * - delivery destination coordinates
 * - the assigned rider and their live position
 */
public record OrderTrackingDto(
        UUID orderId,
        String status,
        String restaurantName,
        BigDecimal restaurantLatitude,
        BigDecimal restaurantLongitude,
        BigDecimal destinationLatitude,
        BigDecimal destinationLongitude,
        boolean riderAssigned,
        UUID riderId,
        String riderName,
        String riderPhoneNumber,
        String vehicleType,
        String licencePlate,
        BigDecimal riderLatitude,
        BigDecimal riderLongitude,
        LocalDateTime riderLocationUpdatedAt,
        String deliveryStatus,
        LocalDateTime requestedAt,
        LocalDateTime pickedUpAt,
        LocalDateTime deliveredAt
) {
}
