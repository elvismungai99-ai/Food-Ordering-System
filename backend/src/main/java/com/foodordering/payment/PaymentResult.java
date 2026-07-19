package com.foodordering.payment;

public class PaymentResult {

    private final boolean successful;
    private final String reference;
    private final String message;

    public PaymentResult(
            boolean successful,
            String reference,
            String message
    ) {
        this.successful = successful;
        this.reference = reference;
        this.message = message;
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
}