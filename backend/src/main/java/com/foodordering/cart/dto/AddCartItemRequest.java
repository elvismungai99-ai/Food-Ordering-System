package com.foodordering.cart.dto;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AddCartItemRequest {

    @NotNull(
            message = "Menu item ID is required"
    )
    private UUID menuItemId;

    @NotNull(
            message = "Quantity is required"
    )
    @Min(
            value = 1,
            message = "Quantity must be at least 1"
    )
    @Max(
            value = 99,
            message = "Quantity cannot exceed 99"
    )
    private Integer quantity;

    public UUID getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(
            UUID menuItemId
    ) {
        this.menuItemId =
                menuItemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(
            Integer quantity
    ) {
        this.quantity =
                quantity;
    }
}