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

    private String selectedSize;
    private java.util.List<String> selectedAddOns = new java.util.ArrayList<>();
    private String specialInstructions;
    private java.util.List<String> removalRequests = new java.util.ArrayList<>();
    private java.math.BigDecimal extraPrice = java.math.BigDecimal.ZERO;

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

    public String getSelectedSize() {
        return selectedSize;
    }

    public void setSelectedSize(String selectedSize) {
        this.selectedSize = selectedSize;
    }

    public java.util.List<String> getSelectedAddOns() {
        return selectedAddOns;
    }

    public void setSelectedAddOns(java.util.List<String> selectedAddOns) {
        this.selectedAddOns = selectedAddOns != null ? selectedAddOns : new java.util.ArrayList<>();
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public java.util.List<String> getRemovalRequests() {
        return removalRequests;
    }

    public void setRemovalRequests(java.util.List<String> removalRequests) {
        this.removalRequests = removalRequests != null ? removalRequests : new java.util.ArrayList<>();
    }

    public java.math.BigDecimal getExtraPrice() {
        return extraPrice;
    }

    public void setExtraPrice(java.math.BigDecimal extraPrice) {
        this.extraPrice = extraPrice != null ? extraPrice : java.math.BigDecimal.ZERO;
    }
}