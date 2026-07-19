package com.foodordering.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodordering.cart.Cart;
import com.foodordering.cart.CartItem;
import com.foodordering.cart.CartRepository;
import com.foodordering.menu.MenuItem;
import com.foodordering.menu.MenuItemRepository;
import com.foodordering.order.dto.OrderDto;
import com.foodordering.order.dto.PlaceOrderRequest;
import com.foodordering.payment.PaymentResult;
import com.foodordering.payment.PaymentService;
import com.foodordering.restaurant.Restaurant;
import com.foodordering.restaurant.RestaurantRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final PaymentService paymentService;
    private final OrderStateMachine orderStateMachine;

    public OrderService(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            MenuItemRepository menuItemRepository,
            RestaurantRepository restaurantRepository,
            PaymentService paymentService,
            OrderStateMachine orderStateMachine
    ) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
        this.paymentService = paymentService;
        this.orderStateMachine = orderStateMachine;
    }

    // =========================================================
    // PLACE ORDER
    // =========================================================

    @Transactional
    public OrderDto placeOrder(
            UUID customerId,
            PlaceOrderRequest request
    ) {

        if (customerId == null) {
            throw new RuntimeException(
                    "Customer ID is required"
            );
        }

        validatePlaceOrderRequest(request);

        // Load customer's cart.
        Cart cart = cartRepository
                .findWithItemsByCustomerId(customerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Cart not found"
                        )
                );

        // Cart must contain at least one item.
        if (
                cart.getItems() == null
                || cart.getItems().isEmpty()
        ) {
            throw new RuntimeException(
                    "Your cart is empty"
            );
        }

        // Load current menu items.
        List<MenuItem> menuItems =
                new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {

            MenuItem menuItem =
                    menuItemRepository
                            .findById(
                                    cartItem.getMenuItemId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "A menu item in your cart no longer exists"
                                    )
                            );

            if (!menuItem.isAvailable()) {
                throw new RuntimeException(
                        menuItem.getName()
                        + " is no longer available"
                );
            }

            menuItems.add(menuItem);
        }

        // All items must belong to one restaurant.
        Set<UUID> restaurantIds =
                menuItems
                        .stream()
                        .map(
                                MenuItem::getRestaurantId
                        )
                        .collect(
                                Collectors.toSet()
                        );

        if (restaurantIds.size() != 1) {
            throw new RuntimeException(
                    "All items in the cart must belong to the same restaurant"
            );
        }

        UUID restaurantId =
                restaurantIds
                        .iterator()
                        .next();

        Restaurant restaurant =
                restaurantRepository
                        .findById(restaurantId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Restaurant not found"
                                )
                        );

        // Make sure the customer has accepted
        // the latest menu prices.
        validateCartPrices(
                cart,
                menuItems
        );

        BigDecimal orderTotal =
                calculateOrderTotal(cart);

        // Simulated payment.
        PaymentResult paymentResult =
                paymentService
                        .processPayment(
                                customerId,
                                orderTotal
                        );

        if (
                paymentResult == null
                || !paymentResult.isSuccessful()
        ) {

            String paymentMessage =
                    paymentResult != null
                            ? paymentResult.getMessage()
                            : "Unknown payment error";

            throw new RuntimeException(
                    "Payment failed: "
                    + paymentMessage
            );
        }

        // Create order.
        Order order =
                new Order();

        order.setCustomerId(
                customerId
        );

        order.setRestaurantId(
                restaurantId
        );

        order.setRestaurantName(
                restaurant.getName()
        );

        order.setDeliveryAddress(
                request
                        .getDeliveryAddress()
                        .trim()
        );

        /*
         * Every newly placed order starts here.
         *
         * Future status changes must go through
         * OrderStateMachine.
         */
        order.setStatus(
                OrderStatus.PENDING
        );

        order.setPaymentStatus(
                PaymentStatus.PAID
        );

        order.setPaymentReference(
                paymentResult.getReference()
        );

        order.setTotalAmount(
                orderTotal
        );

        // Create permanent OrderItem snapshots.
        for (CartItem cartItem : cart.getItems()) {

            MenuItem menuItem =
                    findMenuItem(
                            menuItems,
                            cartItem.getMenuItemId()
                    );

            BigDecimal subtotal =
                    cartItem
                            .getUnitPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            cartItem.getQuantity()
                                    )
                            );

            OrderItem orderItem =
                    new OrderItem();

            orderItem.setMenuItemId(
                    menuItem.getId()
            );

            orderItem.setItemName(
                    menuItem.getName()
            );

            orderItem.setItemDescription(
                    menuItem.getDescription()
            );

            orderItem.setImageUrl(
                    menuItem.getImageUrl()
            );

            orderItem.setQuantity(
                    cartItem.getQuantity()
            );

            // Final accepted purchase price snapshot.
            orderItem.setUnitPrice(
                    cartItem.getUnitPrice()
            );

            orderItem.setSubtotal(
                    subtotal
            );

            order.addItem(
                    orderItem
            );
        }

        // Cascade saves OrderItems.
        Order savedOrder =
                orderRepository.save(order);

        // Clear cart only after successful order creation/payment.
        cart.clearItems();

        cartRepository.save(cart);

        return new OrderDto(
                savedOrder
        );
    }

    // =========================================================
    // UPDATE ORDER STATUS
    // =========================================================

    @Transactional
    public OrderDto updateOrderStatus(
            UUID orderId,
            OrderStatus newStatus
    ) {

        if (orderId == null) {
            throw new RuntimeException(
                    "Order ID is required"
            );
        }

        if (newStatus == null) {
            throw new RuntimeException(
                    "New order status is required"
            );
        }

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Order not found"
                                )
                        );

        OrderStatus currentStatus =
                order.getStatus();

        /*
         * Examples:
         *
         * PENDING -> CONFIRMED      allowed
         * CONFIRMED -> PREPARING   allowed
         * PENDING -> DELIVERED     rejected
         */
        orderStateMachine
                .validateTransition(
                        currentStatus,
                        newStatus
                );

        order.setStatus(
                newStatus
        );

        Order savedOrder =
                orderRepository.save(order);

        return new OrderDto(
                savedOrder
        );
    }

    // =========================================================
    // GET CUSTOMER ORDERS
    // =========================================================

    @Transactional(readOnly = true)
    public List<OrderDto> getCustomerOrders(
            UUID customerId
    ) {

        return orderRepository
                .findByCustomerIdOrderByCreatedAtDesc(
                        customerId
                )
                .stream()
                .map(OrderDto::new)
                .toList();
    }

    // =========================================================
    // GET ONE CUSTOMER ORDER
    // =========================================================

    @Transactional(readOnly = true)
    public OrderDto getCustomerOrder(
            UUID customerId,
            UUID orderId
    ) {

        Order order =
                orderRepository
                        .findByIdAndCustomerId(
                                orderId,
                                customerId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Order not found"
                                )
                        );

        return new OrderDto(
                order
        );
    }

    // =========================================================
    // GET RESTAURANT ORDERS
    // =========================================================

    @Transactional(readOnly = true)
    public List<OrderDto> getRestaurantOrders(
            UUID restaurantId
    ) {

        if (restaurantId == null) {
            throw new RuntimeException(
                    "Restaurant ID is required"
            );
        }

        return orderRepository
                .findByRestaurantIdOrderByCreatedAtDesc(
                        restaurantId
                )
                .stream()
                .map(OrderDto::new)
                .toList();
    }

    // =========================================================
    // VALIDATE PLACE ORDER REQUEST
    // =========================================================

    private void validatePlaceOrderRequest(
            PlaceOrderRequest request
    ) {

        if (request == null) {
            throw new RuntimeException(
                    "Order request is required"
            );
        }

        if (
                request.getDeliveryAddress() == null
                || request
                        .getDeliveryAddress()
                        .isBlank()
        ) {
            throw new RuntimeException(
                    "Delivery address is required"
            );
        }
    }

    // =========================================================
    // VALIDATE CART PRICES
    // =========================================================

    private void validateCartPrices(
            Cart cart,
            List<MenuItem> menuItems
    ) {

        for (CartItem cartItem : cart.getItems()) {

            MenuItem menuItem =
                    findMenuItem(
                            menuItems,
                            cartItem.getMenuItemId()
                    );

            if (
                    cartItem.getUnitPrice() == null
                    || menuItem.getPrice() == null
            ) {
                throw new RuntimeException(
                        "Unable to validate the price of "
                        + menuItem.getName()
                );
            }

            if (
                    cartItem
                            .getUnitPrice()
                            .compareTo(
                                    menuItem.getPrice()
                            ) != 0
            ) {
                throw new RuntimeException(
                        "The price of "
                        + menuItem.getName()
                        + " changed from "
                        + cartItem.getUnitPrice()
                        + " to "
                        + menuItem.getPrice()
                        + ". Please review and accept "
                        + "the updated price before checkout."
                );
            }
        }
    }

    // =========================================================
    // CALCULATE ORDER TOTAL
    // =========================================================

    private BigDecimal calculateOrderTotal(
            Cart cart
    ) {

        BigDecimal total =
                BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {

            if (
                    cartItem.getQuantity() == null
                    || cartItem.getQuantity() <= 0
            ) {
                throw new RuntimeException(
                        "Cart item quantity must be greater than zero"
                );
            }

            if (cartItem.getUnitPrice() == null) {
                throw new RuntimeException(
                        "Cart item price is missing"
                );
            }

            BigDecimal subtotal =
                    cartItem
                            .getUnitPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            cartItem.getQuantity()
                                    )
                            );

            total =
                    total.add(subtotal);
        }

        return total;
    }

    // =========================================================
    // FIND MENU ITEM
    // =========================================================

    private MenuItem findMenuItem(
            List<MenuItem> menuItems,
            UUID menuItemId
    ) {

        return menuItems
                .stream()
                .filter(
                        menuItem ->
                                menuItem
                                        .getId()
                                        .equals(menuItemId)
                )
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "Menu item not found"
                        )
                );
    }
}