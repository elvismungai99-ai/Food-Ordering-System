package com.foodordering.order;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.foodordering.common.exception.BusinessRuleException;

@Component
public class OrderStateMachine {

    private static final Map<
            OrderStatus,
            Set<OrderStatus>
            > ALLOWED_TRANSITIONS =
            Map.of(

                    OrderStatus.PENDING,
                    EnumSet.of(
                            OrderStatus.CONFIRMED,
                            OrderStatus.CANCELLED
                    ),

                    OrderStatus.CONFIRMED,
                    EnumSet.of(
                            OrderStatus.PREPARING,
                            OrderStatus.CANCELLED
                    ),

                    OrderStatus.PREPARING,
                    EnumSet.of(
                            OrderStatus.READY_FOR_PICKUP
                    ),

                    OrderStatus.READY_FOR_PICKUP,
                    EnumSet.of(
                            OrderStatus.OUT_FOR_DELIVERY
                    ),

                    OrderStatus.OUT_FOR_DELIVERY,
                    EnumSet.of(
                            OrderStatus.DELIVERED
                    ),

                    OrderStatus.DELIVERED,
                    EnumSet.noneOf(
                            OrderStatus.class
                    ),

                    OrderStatus.CANCELLED,
                    EnumSet.noneOf(
                            OrderStatus.class
                    )
            );

    public void validateTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus
    ) {

        if (
                currentStatus == null
                || newStatus == null
        ) {
            throw new BusinessRuleException(
                    "Current and new order status are required"
            );
        }

        boolean valid =
                ALLOWED_TRANSITIONS
                        .getOrDefault(
                                currentStatus,
                                EnumSet.noneOf(
                                        OrderStatus.class
                                )
                        )
                        .contains(
                                newStatus
                        );

        if (!valid) {

            throw new BusinessRuleException(
                    "Order cannot move from "
                    + currentStatus
                    + " to "
                    + newStatus
            );
        }
    }
}