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
}