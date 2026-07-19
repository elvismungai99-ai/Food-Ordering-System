package com.foodordering.order.dto;

public class PlaceOrderRequest {

    private String deliveryAddress;

    public PlaceOrderRequest() {
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(
            String deliveryAddress
    ) {
        this.deliveryAddress =
                deliveryAddress;
    }
}