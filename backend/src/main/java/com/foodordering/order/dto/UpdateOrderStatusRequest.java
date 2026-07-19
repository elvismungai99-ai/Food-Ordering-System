package com.foodordering.order.dto;

import com.foodordering.order.OrderStatus;

public class UpdateOrderStatusRequest {

    private OrderStatus status;

    public UpdateOrderStatusRequest() {
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(
            OrderStatus status
    ) {
        this.status = status;
    }
}