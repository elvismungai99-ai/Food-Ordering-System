package com.foodordering.order.dto;

import java.math.BigDecimal;

import com.foodordering.payment.PaymentMethod;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

public class PlaceOrderRequest {

    @NotBlank(
            message = "Delivery address is required"
    )
    @Size(
            max = 500,
            message = "Delivery address must not exceed 500 characters"
    )
    private String deliveryAddress;

    @DecimalMin(
            value = "-90.0",
            message = "Delivery latitude must be at least -90"
    )
    @DecimalMax(
            value = "90.0",
            message = "Delivery latitude must not exceed 90"
    )
    private BigDecimal deliveryLatitude;

    @DecimalMin(
            value = "-180.0",
            message = "Delivery longitude must be at least -180"
    )
    @DecimalMax(
            value = "180.0",
            message = "Delivery longitude must not exceed 180"
    )
    private BigDecimal deliveryLongitude;

    private PaymentMethod paymentMethod;

    @Size(
            max = 20,
            message = "M-Pesa phone number must not exceed 20 characters"
    )
    private String mpesaPhoneNumber;

    @Size(
            max = 128,
            message = "Idempotency key must not exceed 128 characters"
    )
    private String idempotencyKey;

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
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

    public BigDecimal getDeliveryLatitude() {
        return deliveryLatitude;
    }

    public void setDeliveryLatitude(
            BigDecimal deliveryLatitude
    ) {
        this.deliveryLatitude =
                deliveryLatitude;
    }

    public BigDecimal getDeliveryLongitude() {
        return deliveryLongitude;
    }

    public void setDeliveryLongitude(
            BigDecimal deliveryLongitude
    ) {
        this.deliveryLongitude =
                deliveryLongitude;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(
            PaymentMethod paymentMethod
    ) {
        this.paymentMethod =
                paymentMethod;
    }

    public String getMpesaPhoneNumber() {
        return mpesaPhoneNumber;
    }

    public void setMpesaPhoneNumber(
            String mpesaPhoneNumber
    ) {
        this.mpesaPhoneNumber =
                mpesaPhoneNumber;
    }

}
