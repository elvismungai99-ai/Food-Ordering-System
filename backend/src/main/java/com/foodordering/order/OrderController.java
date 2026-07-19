package com.foodordering.order;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodordering.order.dto.OrderDto;
import com.foodordering.order.dto.PlaceOrderRequest;
import com.foodordering.order.dto.UpdateOrderStatusRequest;
import com.foodordering.security.JwtUtil;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    public OrderController(
            OrderService orderService,
            JwtUtil jwtUtil
    ) {
        this.orderService =
                orderService;

        this.jwtUtil =
                jwtUtil;
    }

    // =========================================================
    // PLACE ORDER
    // =========================================================

    /*
     * Customer places an order from their current cart.
     *
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<?> placeOrder(
            @RequestHeader("Authorization")
            String authHeader,

            @RequestBody
            PlaceOrderRequest request
    ) {

        try {

            UUID customerId =
                    extractUserId(
                            authHeader
                    );

            OrderDto order =
                    orderService.placeOrder(
                            customerId,
                            request
                    );

            return ResponseEntity
                    .status(
                            HttpStatus.CREATED
                    )
                    .body(order);

        } catch (
                RuntimeException exception
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            exception.getMessage()
                    );
        }
    }

    // =========================================================
    // GET CUSTOMER ORDERS
    // =========================================================

    /*
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<?> getCustomerOrders(
            @RequestHeader("Authorization")
            String authHeader
    ) {

        try {

            UUID customerId =
                    extractUserId(
                            authHeader
                    );

            List<OrderDto> orders =
                    orderService
                            .getCustomerOrders(
                                    customerId
                            );

            return ResponseEntity
                    .ok(orders);

        } catch (
                RuntimeException exception
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            exception.getMessage()
                    );
        }
    }

    // =========================================================
    // GET RESTAURANT ORDERS
    // =========================================================

    /*
     * Restaurant admin uses this endpoint to load
     * orders belonging to their restaurant.
     *
     * GET /api/orders/restaurant/{restaurantId}
     */
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<?> getRestaurantOrders(
            @PathVariable
            UUID restaurantId
    ) {

        try {

            List<OrderDto> orders =
                    orderService
                            .getRestaurantOrders(
                                    restaurantId
                            );

            return ResponseEntity
                    .ok(orders);

        } catch (
                RuntimeException exception
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            exception.getMessage()
                    );
        }
    }

    // =========================================================
    // GET SINGLE CUSTOMER ORDER
    // =========================================================

    /*
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getCustomerOrder(
            @RequestHeader("Authorization")
            String authHeader,

            @PathVariable
            UUID orderId
    ) {

        try {

            UUID customerId =
                    extractUserId(
                            authHeader
                    );

            OrderDto order =
                    orderService
                            .getCustomerOrder(
                                    customerId,
                                    orderId
                            );

            return ResponseEntity
                    .ok(order);

        } catch (
                RuntimeException exception
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            exception.getMessage()
                    );
        }
    }

    // =========================================================
    // UPDATE ORDER STATUS
    // =========================================================

    /*
     * Restaurant admin uses this endpoint to move an order
     * through the state machine.
     *
     * PATCH /api/orders/{orderId}/status
     *
     * Body:
     *
     * {
     *     "status": "CONFIRMED"
     * }
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable
            UUID orderId,

            @RequestBody
            UpdateOrderStatusRequest request
    ) {

        try {

            if (
                    request == null
                    || request.getStatus() == null
            ) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "Order status is required"
                        );
            }

            OrderDto updatedOrder =
                    orderService
                            .updateOrderStatus(
                                    orderId,
                                    request.getStatus()
                            );

            return ResponseEntity
                    .ok(
                            updatedOrder
                    );

        } catch (
                IllegalStateException exception
        ) {

            /*
             * Example:
             *
             * PENDING -> DELIVERED
             *
             * will be rejected here.
             */
            return ResponseEntity
                    .badRequest()
                    .body(
                            exception.getMessage()
                    );

        } catch (
                RuntimeException exception
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            exception.getMessage()
                    );
        }
    }

    // =========================================================
    // EXTRACT USER ID FROM JWT
    // =========================================================

    private UUID extractUserId(
            String authHeader
    ) {

        if (
                authHeader == null
                || !authHeader
                        .startsWith(
                                "Bearer "
                        )
        ) {

            throw new RuntimeException(
                    "Authorization token is missing or invalid"
            );
        }

        String token =
                authHeader.substring(7);

        return jwtUtil
                .extractUserId(token);
    }
}