package com.foodordering.rider;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRequestRepository
        extends JpaRepository<DeliveryRequest, UUID> {

    boolean existsByOrderId(UUID orderId);

    boolean existsByOrderIdAndStatusNotIn(
            UUID orderId,
            Collection<DeliveryRequestStatus> statuses
    );

    boolean existsByRiderIdAndStatusNotIn(
            UUID riderId,
            Collection<DeliveryRequestStatus> statuses
    );

    @EntityGraph(attributePaths = {})
    List<DeliveryRequest> findByRiderIdOrderByRequestedAtDesc(UUID riderId);

    List<DeliveryRequest> findAllByOrderByRequestedAtDesc();

    Optional<DeliveryRequest> findByIdAndRiderId(
            UUID id,
            UUID riderId
    );

    List<DeliveryRequest> findByRestaurantIdOrderByRequestedAtDesc(
            UUID restaurantId
    );

    long countByRiderIdAndStatus(
            UUID riderId,
            DeliveryRequestStatus status
    );
}
