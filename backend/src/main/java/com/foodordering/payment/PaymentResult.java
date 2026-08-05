package com.foodordering.payment;

import com.foodordering.order.PaymentStatus;

public class PaymentResult {

    private final boolean successful;
    private final String reference;
    private final String message;
    private final PaymentStatus paymentStatus;

    public PaymentResult(
            boolean successful,
            String reference,
            String message
    ) {
        this(
                successful,
                reference,
                message,
                successful
                        ? PaymentStatus.PAID
                        : PaymentStatus.FAILED
        );
    }

    public PaymentResult(
            boolean successful,
            String reference,
            String message,
            PaymentStatus paymentStatus
    ) {
        this.successful = successful;
        this.reference = reference;
        this.message = message;
        this.paymentStatus = paymentStatus;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getReference() {
        return reference;
    }

    public String getMessage() {
        return message;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
}
