package com.foodordering.review;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository
        extends JpaRepository<Review, UUID> {

    boolean existsByOrderIdAndCustomerIdAndRestaurantIdAndMenuItemId(
            UUID orderId,
            UUID customerId,
            UUID restaurantId,
            UUID menuItemId
    );

    Optional<Review> findByOrderIdAndCustomerIdAndRestaurantIdAndMenuItemId(
            UUID orderId,
            UUID customerId,
            UUID restaurantId,
            UUID menuItemId
    );

    boolean existsByOrderIdAndCustomerIdAndRestaurantIdAndMenuItemIdIsNull(
            UUID orderId,
            UUID customerId,
            UUID restaurantId
    );

    List<Review> findByRestaurantIdAndMenuItemIdIsNullOrderByCreatedAtDesc(
            UUID restaurantId
    );

    List<Review> findByMenuItemIdOrderByCreatedAtDesc(
            UUID menuItemId
    );

    @Query("""
            SELECT COALESCE(AVG(r.rating), 0)
            FROM Review r
            WHERE r.restaurantId = :restaurantId
              AND r.menuItemId IS NULL
            """)
    Double getAverageRestaurantRating(
            @Param("restaurantId")
            UUID restaurantId
    );

    long countByRestaurantIdAndMenuItemIdIsNull(
            UUID restaurantId
    );

    @Query("""
            SELECT COALESCE(AVG(r.rating), 0)
            FROM Review r
            WHERE r.menuItemId = :menuItemId
            """)
    Double getAverageMenuItemRating(
            @Param("menuItemId")
            UUID menuItemId
    );

    long countByMenuItemId(
            UUID menuItemId
    );
}
