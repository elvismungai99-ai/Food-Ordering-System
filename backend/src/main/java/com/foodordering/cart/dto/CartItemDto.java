package com.foodordering.cart.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.foodordering.cart.CartItem;
import com.foodordering.menu.MenuItem;

public class CartItemDto {

    private UUID id;
    private UUID menuItemId;
    private String name;
    private String description;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private boolean available;

    public CartItemDto() {
    }

    public CartItemDto(
            CartItem cartItem,
            MenuItem menuItem
    ) {
        this.id = cartItem.getId();
        this.menuItemId = cartItem.getMenuItemId();
        this.quantity = cartItem.getQuantity();
        this.unitPrice = cartItem.getUnitPrice();
        this.subtotal = cartItem.calculateSubtotal();

        if (menuItem != null) {
            this.name = menuItem.getName();
            this.description = menuItem.getDescription();
            this.imageUrl = menuItem.getImageUrl();
            this.available = menuItem.isAvailable();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(UUID menuItemId) {
        this.menuItemId = menuItemId;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}