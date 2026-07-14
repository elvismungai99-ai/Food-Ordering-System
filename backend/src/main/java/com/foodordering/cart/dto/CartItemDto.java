package com.foodordering.cart.dto;

import com.foodordering.cart.CartItem;
import com.foodordering.menu.MenuItem;

import java.math.BigDecimal;
import java.util.UUID;

public class CartItemDto {

    private UUID id;
    private UUID menuItemId;

    private String name;
    private String description;
    private String imageUrl;

    private Integer quantity;

    /*
     * Price stored in the cart when the customer
     * added or last accepted the item.
     */
    private BigDecimal unitPrice;

    /*
     * Current price in the restaurant menu.
     */
    private BigDecimal currentPrice;

    /*
     * Total based on the cart's stored price.
     */
    private BigDecimal subtotal;

    /*
     * Total based on the current restaurant price.
     */
    private BigDecimal currentSubtotal;

    private boolean priceChanged;
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

        this.subtotal = calculateSubtotal(
                cartItem.getUnitPrice(),
                cartItem.getQuantity()
        );

        if (menuItem != null) {
            this.name = menuItem.getName();
            this.description = menuItem.getDescription();
            this.imageUrl = menuItem.getImageUrl();
            this.available = menuItem.isAvailable();

            this.currentPrice = menuItem.getPrice();

            this.currentSubtotal = calculateSubtotal(
                    menuItem.getPrice(),
                    cartItem.getQuantity()
            );

            this.priceChanged =
                    cartItem.getUnitPrice() != null
                    && menuItem.getPrice() != null
                    && cartItem.getUnitPrice()
                            .compareTo(menuItem.getPrice()) != 0;
        } else {
            this.available = false;
            this.currentPrice = cartItem.getUnitPrice();
            this.currentSubtotal = this.subtotal;
            this.priceChanged = false;
        }
    }

    private BigDecimal calculateSubtotal(
            BigDecimal price,
            Integer quantity
    ) {
        if (price == null || quantity == null) {
            return BigDecimal.ZERO;
        }

        return price.multiply(
                BigDecimal.valueOf(quantity)
        );
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

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getCurrentSubtotal() {
        return currentSubtotal;
    }

    public void setCurrentSubtotal(
            BigDecimal currentSubtotal
    ) {
        this.currentSubtotal = currentSubtotal;
    }

    public boolean isPriceChanged() {
        return priceChanged;
    }

    public void setPriceChanged(
            boolean priceChanged
    ) {
        this.priceChanged = priceChanged;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}