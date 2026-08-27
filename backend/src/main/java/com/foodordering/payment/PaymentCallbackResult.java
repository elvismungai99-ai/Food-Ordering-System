package com.foodordering.payment;

public class PaymentCallbackResult {

    private final boolean successful;
    private final String status;
    private final String message;
    private final boolean idempotent;

    public PaymentCallbackResult(
            boolean successful,
            String status,
            String message,
            boolean idempotent
    ) {
        this.successful = successful;
        this.status = status;
        this.message = message;
        this.idempotent = idempotent;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public boolean isIdempotent() {
        return idempotent;
    }
}

