package com.foodordering.order;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

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
                    EnumSet.noneOf(OrderStatus.class),

                    OrderStatus.CANCELLED,
                    EnumSet.noneOf(OrderStatus.class)
            );

    public boolean canTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus
    ) {

        if (
                currentStatus == null
                || newStatus == null
        ) {
            return false;
        }

        return ALLOWED_TRANSITIONS
                .getOrDefault(
                        currentStatus,
                        EnumSet.noneOf(
                                OrderStatus.class
                        )
                )
                .contains(newStatus);
    }

    public void validateTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus
    ) {

        if (
                !canTransition(
                        currentStatus,
                        newStatus
                )
        ) {

            throw new IllegalStateException(
                    "Invalid order status transition from "
                    + currentStatus
                    + " to "
                    + newStatus
            );
        }
    }
}