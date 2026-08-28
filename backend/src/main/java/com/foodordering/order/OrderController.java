package com.foodordering.order;

import java.util.List;
import java.util.UUID;

import com.foodordering.User.entity.User;
import com.foodordering.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodordering.order.dto.OrderDto;
import com.foodordering.order.dto.CancelOrderRequest;
import com.foodordering.order.dto.OrderTrackingDto;
import com.foodordering.order.dto.PlaceOrderRequest;
import com.foodordering.order.dto.UpdateOrderStatusRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderTrackingService orderTrackingService;
    private final SecurityUtils securityUtils;

    public OrderController(
            OrderService orderService,
            OrderTrackingService orderTrackingService,
            SecurityUtils securityUtils
    ) {
        this.orderService = orderService;
        this.orderTrackingService = orderTrackingService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<OrderDto> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request
    ) {
        User customer = securityUtils.requireCustomer();

        OrderDto order = orderService.placeOrder(
                customer.getId(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getCustomerOrders() {
        User customer = securityUtils.requireCustomer();

        return ResponseEntity.ok(
                orderService.getCustomerOrders(customer.getId())
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getCustomerOrder(
            @PathVariable UUID orderId
    ) {
        User customer = securityUtils.requireCustomer();

        return ResponseEntity.ok(
                orderService.getCustomerOrder(
                        customer.getId(),
                        orderId
                )
        );
    }

    @GetMapping("/{orderId}/tracking")
    public ResponseEntity<OrderTrackingDto> getOrderTracking(
            @PathVariable UUID orderId
    ) {
        User customer = securityUtils.requireCustomer();

        return ResponseEntity.ok(
                orderTrackingService.getTracking(
                        customer.getId(),
                        orderId
                )
        );
    }

    @PostMapping("/{orderId}/retry-payment")
    public ResponseEntity<OrderDto> retryPayment(
            @PathVariable UUID orderId,
            @RequestBody(required = false) com.foodordering.order.dto.RetryPaymentRequest request
    ) {
        User customer = securityUtils.requireCustomer();

        com.foodordering.payment.PaymentMethod method =
                request != null ? request.getPaymentMethod() : com.foodordering.payment.PaymentMethod.MPESA;
        String phone = request != null ? request.getMpesaPhoneNumber() : null;

        return ResponseEntity.ok(
                orderService.retryPayment(customer.getId(), orderId, method, phone)
        );
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<OrderDto>> getRestaurantOrders(
            @PathVariable UUID restaurantId
    ) {
        User owner = securityUtils.requireOwner();

        return ResponseEntity.ok(
                orderService.getRestaurantOrders(
                        owner.getId(),
                        restaurantId,
                        securityUtils.isSuperAdmin()
                )
        );
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        User owner = securityUtils.requireOwner();

        return ResponseEntity.ok(
                orderService.updateOrderStatus(
                        owner.getId(),
                        orderId,
                        request.getStatus(),
                        securityUtils.isSuperAdmin()
                )
        );
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDto> cancelCustomerOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody CancelOrderRequest request
    ) {
        User customer = securityUtils.requireCustomer();

        return ResponseEntity.ok(
                orderService.cancelCustomerOrder(
                        customer.getId(),
                        orderId,
                        request.getReason()
                )
        );
    }

    @PatchMapping("/{orderId}/restaurant-cancel")
    public ResponseEntity<OrderDto> cancelRestaurantOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody CancelOrderRequest request
    ) {
        User owner = securityUtils.requireOwner();

        return ResponseEntity.ok(
                orderService.cancelRestaurantOrder(
                        owner.getId(),
                        orderId,
                        request.getReason()
                )
        );
    }
}
