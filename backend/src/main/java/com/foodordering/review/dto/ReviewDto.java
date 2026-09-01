package com.foodordering.review.dto;

import java.util.UUID;

import com.foodordering.review.Review;

public class ReviewDto {

    private UUID id;
    private UUID restaurantId;
    private UUID menuItemId;
    private Integer rating;
    private String comment;
    private String createdAt;
    private String customerDisplayName;

    public ReviewDto() {
    }

    public ReviewDto(Review review) {
        this(review, "Verified Customer");
    }

    public ReviewDto(Review review, String customerDisplayName) {
        id = review.getId();
        restaurantId = review.getRestaurantId();
        menuItemId = review.getMenuItemId();
        rating = review.getRating();
        comment = review.getComment();
        this.customerDisplayName = customerDisplayName != null ? customerDisplayName : "Verified Customer";
        createdAt = review.getCreatedAt() != null
                ? review.getCreatedAt().toString()
                : null;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(UUID restaurantId) {
        this.restaurantId = restaurantId;
    }

    public UUID getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(UUID menuItemId) {
        this.menuItemId = menuItemId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCustomerDisplayName() {
        return customerDisplayName;
    }

    public void setCustomerDisplayName(String customerDisplayName) {
        this.customerDisplayName = customerDisplayName;
    }
}
