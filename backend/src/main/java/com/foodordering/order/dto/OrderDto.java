package com.foodordering.order.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.foodordering.order.Order;
import com.foodordering.order.OrderStatus;
import com.foodordering.order.PaymentStatus;
import com.foodordering.payment.PaymentMethod;

public class OrderDto {

    private UUID id;
    private UUID customerId;
    private UUID restaurantId;

    private String restaurantName;
    private String deliveryAddress;
    private BigDecimal deliveryLatitude;
    private BigDecimal deliveryLongitude;

    private PaymentStatus paymentStatus;
    private String paymentReference;
    private PaymentMethod paymentMethod;
    private String cancellationReason;
    private String cancelledAt;

    private OrderStatus status;
    private BigDecimal totalAmount;
    private BigDecimal subtotalAmount;
    private BigDecimal deliveryFee;
    private BigDecimal serviceFee;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;

    private List<OrderItemDto> items;

    private String createdAt;
    private String updatedAt;

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

        this.deliveryLatitude =
                order.getDeliveryLatitude();

        this.deliveryLongitude =
                order.getDeliveryLongitude();

        this.status = order.getStatus();
        this.totalAmount =
                order.getTotalAmount();
        this.subtotalAmount =
                order.getSubtotalAmount();
        this.deliveryFee =
                order.getDeliveryFee();
        this.serviceFee =
                order.getServiceFee();
        this.taxAmount =
                order.getTaxAmount();
        this.discountAmount =
                order.getDiscountAmount();

        this.items = order.getItems()
                .stream()
                .map(OrderItemDto::new)
                .toList();

        this.createdAt = order.getCreatedAt() != null
                ? order.getCreatedAt().toString()
                : null;
        this.updatedAt = order.getUpdatedAt() != null
                ? order.getUpdatedAt().toString()
                : null;

        this.paymentStatus = order.getPaymentStatus();
        this.paymentReference = order.getPaymentReference();
        this.paymentMethod = order.getPaymentMethod();
        this.cancellationReason = order.getCancellationReason();
        this.cancelledAt = order.getCancelledAt() != null
                ? order.getCancelledAt().toString()
                : null;
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

    public BigDecimal getDeliveryLatitude() {
        return deliveryLatitude;
    }

    public BigDecimal getDeliveryLongitude() {
        return deliveryLongitude;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public BigDecimal getServiceFee() {
        return serviceFee;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public String getCancelledAt() {
        return cancelledAt;
    }
}
