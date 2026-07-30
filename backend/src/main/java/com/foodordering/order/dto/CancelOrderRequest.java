package com.foodordering.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CancelOrderRequest {

    @NotBlank(message = "Cancellation reason is required")
    @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
