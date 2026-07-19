package com.foodordering.payment;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {

    public PaymentResult processPayment(
            UUID customerId,
            BigDecimal amount
    ) {

        if (
                amount == null
                || amount.compareTo(
                        BigDecimal.ZERO
                ) <= 0
        ) {

            return new PaymentResult(
                    false,
                    null,
                    "Invalid payment amount"
            );
        }

        String reference =
                "SIM-"
                + UUID.randomUUID()
                    .toString()
                    .toUpperCase();

        return new PaymentResult(
                true,
                reference,
                "Payment successful"
        );
    }
}