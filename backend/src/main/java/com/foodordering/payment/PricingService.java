package com.foodordering.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    private final BigDecimal deliveryFee;
    private final BigDecimal serviceFee;
    private final BigDecimal taxRate;
    private final BigDecimal discountAmount;

    public PricingService(
            @Value("${payments.pricing.delivery-fee:150.00}")
            BigDecimal deliveryFee,
            @Value("${payments.pricing.service-fee:35.00}")
            BigDecimal serviceFee,
            @Value("${payments.pricing.tax-rate:0.00}")
            BigDecimal taxRate,
            @Value("${payments.pricing.discount-amount:0.00}")
            BigDecimal discountAmount
    ) {
        this.deliveryFee =
                money(deliveryFee);
        this.serviceFee =
                money(serviceFee);
        this.taxRate =
                taxRate != null
                        ? taxRate
                        : BigDecimal.ZERO;
        this.discountAmount =
                money(discountAmount);
    }

    public PricingBreakdown calculate(
            BigDecimal subtotalAmount
    ) {

        BigDecimal safeSubtotal =
                money(subtotalAmount);

        BigDecimal taxAmount =
                money(
                        safeSubtotal.multiply(
                                taxRate
                        )
                );

        BigDecimal discount =
                discountAmount.min(
                        safeSubtotal
                                .add(deliveryFee)
                                .add(serviceFee)
                                .add(taxAmount)
                );

        BigDecimal total =
                safeSubtotal
                        .add(deliveryFee)
                        .add(serviceFee)
                        .add(taxAmount)
                        .subtract(discount);

        return new PricingBreakdown(
                safeSubtotal,
                deliveryFee,
                serviceFee,
                taxAmount,
                money(discount),
                money(total)
        );
    }

    private BigDecimal money(
            BigDecimal value
    ) {

        return (value != null
                ? value
                : BigDecimal.ZERO)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }
}
