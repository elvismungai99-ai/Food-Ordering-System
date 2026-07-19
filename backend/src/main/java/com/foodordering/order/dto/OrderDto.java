package com.foodordering.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.foodordering.order.Order;
import com.foodordering.order.OrderStatus;
import com.foodordering.order.PaymentStatus;

public class OrderDto {

    private UUID id;
    private UUID customerId;
    private UUID restaurantId;

    private String restaurantName;
    private String deliveryAddress;

    private PaymentStatus paymentStatus;
    private String paymentReference;

    private OrderStatus status;
    private BigDecimal totalAmount;

    private List<OrderItemDto> items;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderDto() {
    }

    public OrderDto(Order order) {
        this.id = order.getId();
        this.customerId = order.getCustomerId();
        this.restaurantId =
                order.getRestaurantId();

        this.restaurantName =
                order.getRestaurantName();

        this.deliveryAddress =
                order.getDeliveryAddress();

        this.status = order.getStatus();
        this.totalAmount =
                order.getTotalAmount();

        this.items = order.getItems()
                .stream()
                .map(OrderItemDto::new)
                .toList();

        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();

        this.paymentStatus = order.getPaymentStatus();
        this.paymentReference = order.getPaymentReference();
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public List<OrderItemDto> getItems() {
        return items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public String getPaymentReference() {
        return paymentReference;
    }
}