package com.foodordering.payment;

import java.math.BigDecimal;

public record PricingBreakdown(
        BigDecimal subtotalAmount,
        BigDecimal deliveryFee,
        BigDecimal serviceFee,
        BigDecimal taxAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount
) {
}
