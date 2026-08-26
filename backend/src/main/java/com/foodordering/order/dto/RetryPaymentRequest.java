package com.foodordering.order.dto;

import com.foodordering.payment.PaymentMethod;

public class RetryPaymentRequest {
    private PaymentMethod paymentMethod;
    private String mpesaPhoneNumber;

    public RetryPaymentRequest() {
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getMpesaPhoneNumber() {
        return mpesaPhoneNumber;
    }

    public void setMpesaPhoneNumber(String mpesaPhoneNumber) {
        this.mpesaPhoneNumber = mpesaPhoneNumber;
    }
}

