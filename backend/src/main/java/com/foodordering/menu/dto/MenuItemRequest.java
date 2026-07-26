package com.foodordering.menu.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class MenuItemRequest {

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

    @NotNull(
            message = "Availability is required"
    )
    private Boolean available;

    @Size(
            max = 2000,
            message = "Image URL is too long"
    )
    @Pattern(
            regexp = "^$|^https?://.+",
            message = "Image URL must begin with http:// or https://"
    )
    private String imageUrl;

    // getters and setters
}