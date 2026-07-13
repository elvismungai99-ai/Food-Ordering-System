package com.foodordering.menu;

import java.math.BigDecimal;
import java.util.UUID;

public class MenuItemDto {

    private UUID id;
    private UUID restaurantId;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Boolean available;
    private String imageUrl;

    public MenuItemDto() {
    }

    public MenuItemDto(MenuItem menuItem) {
        this.id = menuItem.getId();
        this.restaurantId = menuItem.getRestaurantId();
        this.name = menuItem.getName();
        this.description = menuItem.getDescription();
        this.price = menuItem.getPrice();
        this.category = menuItem.getCategory();
        this.available = menuItem.isAvailable();
        this.imageUrl = menuItem.getImageUrl();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getAvailable() {
        return available;
    }

    public boolean isAvailable() {
        return Boolean.TRUE.equals(available);
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}