package com.foodordering.order.dto;

import com.foodordering.order.OrderStatus;

import jakarta.validation.constraints.NotNull;

public class UpdateOrderStatusRequest {

    @NotNull(
            message = "Order status is required"
    )
    private OrderStatus status;

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(
            OrderStatus status
    ) {
        this.status = status;
    }
}