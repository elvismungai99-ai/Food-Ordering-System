package com.foodordering.order;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodordering.common.exception.ResourceNotFoundException;
import com.foodordering.order.dto.OrderTrackingDto;
import com.foodordering.rider.DeliveryRequest;
import com.foodordering.rider.DeliveryRequestRepository;
import com.foodordering.rider.Rider;
import com.foodordering.rider.RiderRepository;

/*
 * Builds the live-tracking snapshot for
 * one customer order: order status, restaurant
 * pickup point, delivery destination and the
 * assigned rider's most recent GPS position.
 */
@Service
public class OrderTrackingService {

    private final OrderRepository orderRepository;
    private final DeliveryRequestRepository deliveryRequestRepository;
    private final RiderRepository riderRepository;

    public OrderTrackingService(
            OrderRepository orderRepository,
            DeliveryRequestRepository deliveryRequestRepository,
            RiderRepository riderRepository
    ) {

        this.orderRepository =
                orderRepository;

        this.deliveryRequestRepository =
                deliveryRequestRepository;

        this.riderRepository =
                riderRepository;
    }

    @Transactional(readOnly = true)
    public OrderTrackingDto getTracking(
            UUID customerId,
            UUID orderId
    ) {

        Order order = orderRepository
                .findByIdAndCustomerId(
                        orderId,
                        customerId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found"
                        )
                );

        DeliveryRequest deliveryRequest =
                deliveryRequestRepository
                        .findByOrderId(orderId)
                        .orElse(null);

        Rider rider = null;

        if (deliveryRequest != null) {
            rider = riderRepository
                    .findById(
                            deliveryRequest.getRiderId()
                    )
                    .orElse(null);
        }

        return buildTrackingDto(
                order,
                deliveryRequest,
                rider
        );
    }

    private OrderTrackingDto buildTrackingDto(
            Order order,
            DeliveryRequest deliveryRequest,
            Rider rider
    ) {

        boolean hasDeliveryRequest =
                deliveryRequest != null;

        boolean isDeliveryActive = order.getStatus() == OrderStatus.OUT_FOR_DELIVERY
                || order.getStatus() == OrderStatus.READY_FOR_PICKUP
                || order.getStatus() == OrderStatus.PREPARING;

        Integer etaMinutes = switch (order.getStatus()) {
            case PENDING, CONFIRMED -> 35;
            case PREPARING -> 25;
            case READY_FOR_PICKUP -> 15;
            case OUT_FOR_DELIVERY -> 10;
            case DELIVERED, CANCELLED -> 0;
        };

        return new OrderTrackingDto(
                order.getId(),
                order.getStatus().name(),
                order.getPaymentStatus() != null ? order.getPaymentStatus().name() : "PENDING",
                etaMinutes,
                order.getRestaurantName(),
                hasDeliveryRequest
                        ? deliveryRequest.getRestaurantLatitude()
                        : null,
                hasDeliveryRequest
                        ? deliveryRequest.getRestaurantLongitude()
                        : null,
                order.getDeliveryLatitude(),
                order.getDeliveryLongitude(),
                rider != null,
                rider != null
                        ? rider.getId()
                        : null,
                rider != null
                        ? rider.getFullName()
                        : null,
                rider != null && isDeliveryActive
                        ? rider.getPhoneNumber()
                        : null,
                rider != null
                        ? rider.getVehicleType().name()
                        : null,
                rider != null
                        ? rider.getLicencePlate()
                        : null,
                rider != null && isDeliveryActive
                        ? rider.getCurrentLatitude()
                        : null,
                rider != null && isDeliveryActive
                        ? rider.getCurrentLongitude()
                        : null,
                rider != null && isDeliveryActive
                        ? rider.getLastLocationUpdatedAt()
                        : null,
                hasDeliveryRequest
                        ? deliveryRequest.getStatus().name()
                        : null,
                hasDeliveryRequest
                        ? deliveryRequest.getRequestedAt()
                        : null,
                hasDeliveryRequest
                        ? deliveryRequest.getPickedUpAt()
                        : null,
                hasDeliveryRequest
                        ? deliveryRequest.getDeliveredAt()
                        : null
        );
    }
}
