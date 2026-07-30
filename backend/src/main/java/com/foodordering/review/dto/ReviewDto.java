package com.foodordering.review.dto;

import java.util.UUID;

import com.foodordering.review.Review;

public class ReviewDto {

    private UUID id;
    private UUID orderId;
    private UUID customerId;
    private UUID restaurantId;
    private UUID menuItemId;
    private Integer rating;
    private String comment;
    private String createdAt;

    public ReviewDto() {
    }

    public ReviewDto(Review review) {
        id = review.getId();
        orderId = review.getOrderId();
        customerId = review.getCustomerId();
        restaurantId = review.getRestaurantId();
        menuItemId = review.getMenuItemId();
        rating = review.getRating();
        comment = review.getComment();
        createdAt = review.getCreatedAt() != null
                ? review.getCreatedAt().toString()
                : null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public UUID getMenuItemId() {
        return menuItemId;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
