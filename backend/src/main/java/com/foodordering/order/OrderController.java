package com.foodordering.order;

import java.util.List;
import java.util.Locale;
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
import com.foodordering.common.exception.ForbiddenOperationException;
import com.foodordering.security.JwtUtil;

import jakarta.validation.Valid;

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

    @PostMapping
    public ResponseEntity<OrderDto>
    placeOrder(

            @RequestHeader("Authorization")
            String authHeader,

            @Valid
            @RequestBody
            PlaceOrderRequest request
    ) {

        UUID customerId =
                extractUserId(
                        authHeader
                );

        requireCustomer(
                authHeader
        );

        OrderDto order =
                orderService
                        .placeOrder(
                                customerId,
                                request
                        );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>>
    getCustomerOrders(

            @RequestHeader("Authorization")
            String authHeader
    ) {

        UUID customerId =
                extractUserId(
                        authHeader
                );

        requireCustomer(
                authHeader
        );

        return ResponseEntity.ok(
                orderService
                        .getCustomerOrders(
                                customerId
                        )
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto>
    getCustomerOrder(

            @RequestHeader("Authorization")
            String authHeader,

            @PathVariable
            UUID orderId
    ) {

        UUID customerId =
                extractUserId(
                        authHeader
                );

        requireCustomer(
                authHeader
        );

        return ResponseEntity.ok(
                orderService
                        .getCustomerOrder(
                                customerId,
                                orderId
                        )
        );
    }

    @GetMapping(
            "/restaurant/{restaurantId}"
    )
    public ResponseEntity<List<OrderDto>>
    getRestaurantOrders(

            @RequestHeader("Authorization")
            String authHeader,

            @PathVariable
            UUID restaurantId
    ) {

        requireRestaurantOwner(
                authHeader
        );

        return ResponseEntity.ok(
                orderService
                        .getRestaurantOrders(
                                restaurantId
                        )
        );
    }

    @PatchMapping(
            "/{orderId}/status"
    )
    public ResponseEntity<OrderDto>
    updateOrderStatus(

            @RequestHeader("Authorization")
            String authHeader,

            @PathVariable
            UUID orderId,

            @Valid
            @RequestBody
            UpdateOrderStatusRequest request
    ) {

        requireRestaurantOwner(
                authHeader
        );

        return ResponseEntity.ok(
                orderService
                        .updateOrderStatus(
                                orderId,
                                request.getStatus()
                        )
        );
    }

    private UUID extractUserId(
            String authHeader
    ) {

        String token =
                authHeader.substring(7);

        return jwtUtil
                .extractUserId(token);
    }

    private void requireCustomer(
            String authHeader
    ) {

        String role =
                extractRole(
                        authHeader
                );

        if (!"CUSTOMER".equals(role)) {
            throw new ForbiddenOperationException(
                    "Only customers can access customer orders"
            );
        }
    }

    private void requireRestaurantOwner(
            String authHeader
    ) {

        String role =
                extractRole(
                        authHeader
                );

        if (
                !"OWNER".equals(role)
                && !"RESTAURANT_ADMIN".equals(role)
                && !"RESTAURANT_OWNER".equals(role)
                && !"SUPER_ADMIN".equals(role)
        ) {
            throw new ForbiddenOperationException(
                    "Only restaurant owners can access restaurant orders"
            );
        }
    }

    private String extractRole(
            String authHeader
    ) {

        String token =
                authHeader.substring(7);

        return normalizeRole(
                jwtUtil.extractRole(token)
        );
    }

    private String normalizeRole(
            String role
    ) {

        if (role == null || role.isBlank()) {
            return "";
        }

        String normalized =
                role.trim().toUpperCase(Locale.ROOT);

        if (normalized.startsWith("ROLE_")) {
            normalized =
                    normalized.substring(5);
        }

        return normalized;
    }
} 
