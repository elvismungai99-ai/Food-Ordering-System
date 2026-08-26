package com.foodordering.order.dto;

import com.foodordering.order.OrderItem;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItemDto {

    private UUID id;
    private UUID menuItemId;

    private String itemName;
    private String itemDescription;
    private String imageUrl;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    private String selectedSize;
    private java.util.List<String> selectedAddOns = new java.util.ArrayList<>();
    private String specialInstructions;
    private java.util.List<String> removalRequests = new java.util.ArrayList<>();

    public OrderItemDto() {
    }

    public OrderItemDto(OrderItem item) {
        this.id = item.getId();
        this.menuItemId = item.getMenuItemId();
        this.itemName = item.getItemName();
        this.itemDescription =
                item.getItemDescription();
        this.imageUrl = item.getImageUrl();
        this.quantity = item.getQuantity();
        this.unitPrice = item.getUnitPrice();
        this.subtotal = item.getSubtotal();
        this.selectedSize = item.getSelectedSize();
        this.selectedAddOns = parseList(item.getSelectedAddOns());
        this.specialInstructions = item.getSpecialInstructions();
        this.removalRequests = parseList(item.getRemovalRequests());
    }

    private static java.util.List<String> parseList(String raw) {
        if (raw == null || raw.isBlank()) return new java.util.ArrayList<>();
        java.util.List<String> list = new java.util.ArrayList<>();
        for (String s : raw.split(";;")) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) list.add(trimmed);
        }
        return list;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMenuItemId() {
        return menuItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public String getSelectedSize() {
        return selectedSize;
    }

    public java.util.List<String> getSelectedAddOns() {
        return selectedAddOns;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public java.util.List<String> getRemovalRequests() {
        return removalRequests;
    }
}