package com.foodordering.menu;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class MenuItemDto {

    private UUID id;

    private UUID restaurantId;

    @NotBlank(
            message = "Menu item name is required"
    )
    @Size(
            max = 120,
            message = "Menu item name must not exceed 120 characters"
    )
    private String name;

    @Size(
            max = 1000,
            message = "Description must not exceed 1000 characters"
    )
    private String description;

    @NotNull(
            message = "Menu item price is required"
    )
    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "Menu item price cannot be negative"
    )
    private BigDecimal price;

    @Size(
            max = 100,
            message = "Category must not exceed 100 characters"
    )
    private String category;

    private List<String> addOns =
            new ArrayList<>();

    private boolean available;

    @Size(
            max = 5000000,
            message = "Uploaded image is too large (max 5MB)"
    )
    @Pattern(
            regexp = "^$|^https?://.+|^data:image/(jpeg|png|webp|gif);base64,.+",
            message = "Image must be a URL or uploaded JPEG, PNG, WebP, or GIF image"
    )
    private String imageUrl;
    private Double averageRating;
    private long reviewCount;

    public MenuItemDto() {
    }

    /*
     * Converts a MenuItem entity into the DTO
     * returned to React.
     */
    public MenuItemDto(
            MenuItem menuItem
    ) {
        this.id =
                menuItem.getId();

        this.restaurantId =
                menuItem.getRestaurantId();

        this.name =
                menuItem.getName();

        this.description =
                menuItem.getDescription();

        this.price =
                menuItem.getPrice();

        this.category =
                menuItem.getCategory();

        this.addOns =
                new ArrayList<>(
                        menuItem.getAddOns()
                );

        this.available =
                menuItem.isAvailable();

        this.imageUrl =
                menuItem.getImageUrl();
    }

    public UUID getId() {
        return id;
    }

    public void setId(
            UUID id
    ) {
        this.id = id;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(
            UUID restaurantId
    ) {
        this.restaurantId =
                restaurantId;
    }

    public String getName() {
        return name;
    }

    public void setName(
            String name
    ) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description
    ) {
        this.description =
                description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(
            BigDecimal price
    ) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(
            String category
    ) {
        this.category =
                category;
    }

    public List<String> getAddOns() {
        return addOns;
    }

    public void setAddOns(
            List<String> addOns
    ) {
        this.addOns =
                addOns != null
                        ? addOns
                        : new ArrayList<>();
    }

    public boolean isAvailable() {
        return available;
    }

    /*
     * Compatibility getter because some older
     * parts of the project previously used
     * getAvailable().
     */
    public boolean getAvailable() {
        return available;
    }

    public void setAvailable(
            boolean available
    ) {
        this.available =
                available;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(
            String imageUrl
    ) {
        this.imageUrl =
                imageUrl;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(long reviewCount) {
        this.reviewCount = reviewCount;
    }
}
