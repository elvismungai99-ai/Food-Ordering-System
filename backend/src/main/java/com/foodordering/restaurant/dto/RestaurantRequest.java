package com.foodordering.restaurant.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RestaurantRequest {

    @NotBlank(
            message = "Restaurant name is required"
    )
    @Size(
            max = 150,
            message = "Restaurant name must not exceed 150 characters"
    )
    private String name;

    @Size(
            max = 1000,
            message = "Description must not exceed 1000 characters"
    )
    private String description;

    @NotBlank(
            message = "Restaurant address is required"
    )
    @Size(
            max = 500,
            message = "Restaurant address must not exceed 500 characters"
    )
    private String address;

    @NotNull(
            message = "Opening time is required"
    )
    private LocalTime openingTime;

    @NotNull(
            message = "Closing time is required"
    )
    private LocalTime closingTime;

    @NotBlank(
            message = "Restaurant category is required"
    )
    @Size(
            max = 100,
            message = "Category must not exceed 100 characters"
    )
    private String category;

    // getters and setters
}