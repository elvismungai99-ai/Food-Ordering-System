package com.foodordering.cart.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CartDto {

    private UUID id;
    private UUID customerId;

    private List<CartItemDto> items =
            new ArrayList<>();

    private Integer totalItems;

    /*
     * Total using the prices stored in cart_items.
     */
    private BigDecimal previousTotalAmount;

    /*
     * Total using current menu prices.
     * This is the amount that will be charged
     * after the customer accepts the changes.
     */
    private BigDecimal totalAmount;

    private boolean hasPriceChanges;
    private boolean hasUnavailableItems;

    public CartDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(
            List<CartItemDto> items
    ) {
        this.items = items;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(
            Integer totalItems
    ) {
        this.totalItems = totalItems;
    }

    public BigDecimal getPreviousTotalAmount() {
        return previousTotalAmount;
    }

    public void setPreviousTotalAmount(
            BigDecimal previousTotalAmount
    ) {
        this.previousTotalAmount =
                previousTotalAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(
            BigDecimal totalAmount
    ) {
        this.totalAmount = totalAmount;
    }

    public boolean isHasPriceChanges() {
        return hasPriceChanges;
    }

    public void setHasPriceChanges(
            boolean hasPriceChanges
    ) {
        this.hasPriceChanges =
                hasPriceChanges;
    }

    public boolean isHasUnavailableItems() {
        return hasUnavailableItems;
    }

    public void setHasUnavailableItems(
            boolean hasUnavailableItems
    ) {
        this.hasUnavailableItems =
                hasUnavailableItems;
    }
}