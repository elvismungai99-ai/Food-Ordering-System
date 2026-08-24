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

        return new OrderTrackingDto(
                order.getId(),
                order.getStatus().name(),
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
                rider != null
                        ? rider.getPhoneNumber()
                        : null,
                rider != null
                        ? rider.getVehicleType().name()
                        : null,
                rider != null
                        ? rider.getLicencePlate()
                        : null,
                rider != null
                        ? rider.getCurrentLatitude()
                        : null,
                rider != null
                        ? rider.getCurrentLongitude()
                        : null,
                rider != null
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
