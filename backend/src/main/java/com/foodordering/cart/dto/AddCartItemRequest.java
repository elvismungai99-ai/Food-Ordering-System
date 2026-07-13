package com.foodordering.cart.dto;

import java.util.UUID;

public class AddCartItemRequest {

    private UUID menuItemId;
    private Integer quantity;

    public AddCartItemRequest() {
    }

    public UUID getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(UUID menuItemId) {
        this.menuItemId = menuItemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}