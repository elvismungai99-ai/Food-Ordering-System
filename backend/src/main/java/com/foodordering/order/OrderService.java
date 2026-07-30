package com.foodordering.order;

import com.foodordering.cart.Cart;
import com.foodordering.cart.CartItem;
import com.foodordering.cart.CartRepository;

import com.foodordering.common.exception.BusinessRuleException;
import com.foodordering.common.exception.ResourceNotFoundException;

import com.foodordering.menu.MenuItem;
import com.foodordering.menu.MenuItemRepository;

import com.foodordering.order.dto.OrderDto;
import com.foodordering.order.dto.PlaceOrderRequest;

import com.foodordering.payment.PaymentResult;
import com.foodordering.payment.PaymentService;

import com.foodordering.restaurant.Restaurant;
import com.foodordering.restaurant.RestaurantRepository;
import com.foodordering.restaurant.RestaurantService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;
    private final PaymentService paymentService;
    private final OrderStateMachine orderStateMachine;

    public OrderService(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            MenuItemRepository menuItemRepository,
            RestaurantRepository restaurantRepository,
            RestaurantService restaurantService,
            PaymentService paymentService,
            OrderStateMachine orderStateMachine
    ) {
        this.orderRepository =
                orderRepository;

        this.cartRepository =
                cartRepository;

        this.menuItemRepository =
                menuItemRepository;

        this.restaurantRepository =
                restaurantRepository;

        this.restaurantService =
                restaurantService;

        this.paymentService =
                paymentService;

        this.orderStateMachine =
                orderStateMachine;
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
            throw new BusinessRuleException(
                    "Customer ID is required"
            );
        }

        /*
         * @Valid should already reject an empty address,
         * but this keeps the service protected when called
         * from somewhere other than the controller.
         */
        if (
                request == null
                || request.getDeliveryAddress() == null
                || request
                        .getDeliveryAddress()
                        .isBlank()
        ) {
            throw new BusinessRuleException(
                    "Delivery address is required"
            );
        }

        if (
                (request.getDeliveryLatitude() == null)
                != (request.getDeliveryLongitude() == null)
        ) {
            throw new BusinessRuleException(
                    "Both delivery latitude and longitude are required when using current location"
            );
        }

        Cart cart =
                cartRepository
                        .findWithItemsByCustomerId(
                                customerId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Cart not found"
                                )
                        );

        if (
                cart.getItems() == null
                || cart.getItems().isEmpty()
        ) {
            throw new BusinessRuleException(
                    "Your cart is empty"
            );
        }

        List<MenuItem> menuItems =
                new ArrayList<>();

        /*
         * Verify that every item still exists
         * and is still available.
         */
        for (
                CartItem cartItem :
                cart.getItems()
        ) {

            MenuItem menuItem =
                    menuItemRepository
                            .findById(
                                    cartItem
                                            .getMenuItemId()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "A menu item in your cart no longer exists"
                                    )
                            );

            if (!menuItem.isAvailable()) {

                throw new BusinessRuleException(
                        menuItem.getName()
                        + " is no longer available"
                );
            }

            menuItems.add(
                    menuItem
            );
        }

        /*
         * Your current cart supports one
         * restaurant at a time.
         */
        Set<UUID> restaurantIds =
                menuItems
                        .stream()
                        .map(
                                MenuItem::getRestaurantId
                        )
                        .collect(
                                Collectors.toSet()
                        );

        if (
                restaurantIds.size() != 1
        ) {
            throw new BusinessRuleException(
                    "All items in the cart must belong to the same restaurant"
            );
        }

        UUID restaurantId =
                restaurantIds
                        .iterator()
                        .next();

        Restaurant restaurant =
                restaurantRepository
                        .findById(
                                restaurantId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Restaurant not found"
                                )
                        );

        if (!restaurantService.isApprovedAndOpen(restaurant)) {
            throw new BusinessRuleException(
                    "This restaurant is not open for orders right now"
            );
        }

        /*
         * Protect against restaurant price changes.
         */
        validateCartPrices(
                cart,
                menuItems
        );

        BigDecimal orderTotal =
                calculateOrderTotal(
                        cart
                );

        /*
         * Keep your current payment simulation.
         * M-PESA will replace this later.
         */
        PaymentResult paymentResult =
                paymentService
                        .processPayment(
                                customerId,
                                orderTotal
                        );

        if (
                paymentResult == null
                || !paymentResult
                        .isSuccessful()
        ) {

            String message =
                    paymentResult != null
                            ? paymentResult
                                    .getMessage()
                            : "Unknown payment error";

            throw new BusinessRuleException(
                    "Payment failed: "
                    + message
            );
        }

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

        order.setDeliveryLatitude(
                request.getDeliveryLatitude()
        );

        order.setDeliveryLongitude(
                request.getDeliveryLongitude()
        );

        /*
         * New orders begin at PENDING.
         */
        order.setStatus(
                OrderStatus.PENDING
        );

        order.setPaymentStatus(
                PaymentStatus.PAID
        );

        order.setPaymentReference(
                paymentResult
                        .getReference()
        );

        order.setTotalAmount(
                orderTotal
        );

        /*
         * Create permanent order-item snapshots.
         */
        for (
                CartItem cartItem :
                cart.getItems()
        ) {

            MenuItem menuItem =
                    findMenuItem(
                            menuItems,
                            cartItem
                                    .getMenuItemId()
                    );

            BigDecimal subtotal =
                    cartItem
                            .getUnitPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            cartItem
                                                    .getQuantity()
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

            /*
             * This is the accepted purchase-price snapshot.
             */
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

        Order savedOrder =
                orderRepository.save(
                        order
                );

        /*
         * Cart is cleared only after
         * successful order creation/payment.
         */
        cart.clearItems();

        cartRepository.save(
                cart
        );

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
            throw new BusinessRuleException(
                    "Order ID is required"
            );
        }

        if (newStatus == null) {
            throw new BusinessRuleException(
                    "New order status is required"
            );
        }

        if (newStatus == OrderStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "Use the cancellation endpoint so a reason is recorded"
            );
        }

        Order order =
                orderRepository
                        .findById(
                                orderId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found"
                                )
                        );

        orderStateMachine
                .validateTransition(
                        order.getStatus(),
                        newStatus
                );

        order.setStatus(
                newStatus
        );

        Order savedOrder =
                orderRepository.save(
                        order
                );

        return new OrderDto(
                savedOrder
        );
    }

    @Transactional
    public OrderDto cancelCustomerOrder(
            UUID customerId,
            UUID orderId,
            String reason
    ) {

        Order order =
                orderRepository
                        .findByIdAndCustomerId(
                                orderId,
                                customerId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found"
                                )
                        );

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessRuleException(
                    "Customers can cancel only pending orders"
            );
        }

        cancelOrder(order, reason);

        return new OrderDto(
                orderRepository.save(order)
        );
    }

    @Transactional
    public OrderDto cancelRestaurantOrder(
            UUID ownerId,
            UUID orderId,
            String reason
    ) {

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found"
                                )
                        );

        Restaurant restaurant =
                restaurantRepository
                        .findById(order.getRestaurantId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Restaurant not found"
                                )
                        );

        if (
                restaurant.getOwnerId() == null
                || !restaurant.getOwnerId().equals(ownerId)
        ) {
            throw new BusinessRuleException(
                    "You can cancel only orders for your restaurant"
            );
        }

        if (
                order.getStatus() != OrderStatus.PENDING
                && order.getStatus() != OrderStatus.CONFIRMED
        ) {
            throw new BusinessRuleException(
                    "Restaurant can cancel only before preparation starts"
            );
        }

        cancelOrder(order, reason);

        return new OrderDto(
                orderRepository.save(order)
        );
    }

    private void cancelOrder(
            Order order,
            String reason
    ) {

        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleException(
                    "Cancellation reason is required"
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancellationReason(reason.trim());
        order.setCancelledAt(LocalDateTime.now());
    }

    // =========================================================
    // CUSTOMER ORDERS
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
    // ONE CUSTOMER ORDER
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
                                new ResourceNotFoundException(
                                        "Order not found"
                                )
                        );

        return new OrderDto(
                order
        );
    }

    // =========================================================
    // RESTAURANT ORDERS
    // =========================================================

    @Transactional(readOnly = true)
    public List<OrderDto> getRestaurantOrders(
            UUID restaurantId
    ) {

        if (
                !restaurantRepository
                        .existsById(
                                restaurantId
                        )
        ) {
            throw new ResourceNotFoundException(
                    "Restaurant not found"
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
    // PRICE VALIDATION
    // =========================================================

    private void validateCartPrices(
            Cart cart,
            List<MenuItem> menuItems
    ) {

        for (
                CartItem cartItem :
                cart.getItems()
        ) {

            MenuItem menuItem =
                    findMenuItem(
                            menuItems,
                            cartItem
                                    .getMenuItemId()
                    );

            if (
                    cartItem.getUnitPrice() == null
                    || menuItem.getPrice() == null
            ) {

                throw new BusinessRuleException(
                        "Unable to validate the price of "
                        + menuItem.getName()
                );
            }

            if (
                    cartItem
                            .getUnitPrice()
                            .compareTo(
                                    menuItem
                                            .getPrice()
                            ) != 0
            ) {

                throw new BusinessRuleException(
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
    // TOTAL CALCULATION
    // =========================================================

    private BigDecimal calculateOrderTotal(
            Cart cart
    ) {

        BigDecimal total =
                BigDecimal.ZERO;

        for (
                CartItem cartItem :
                cart.getItems()
        ) {

            if (
                    cartItem.getQuantity() == null
                    || cartItem
                            .getQuantity() <= 0
            ) {

                throw new BusinessRuleException(
                        "Cart item quantity must be greater than zero"
                );
            }

            if (
                    cartItem.getUnitPrice()
                    == null
            ) {

                throw new BusinessRuleException(
                        "Cart item price is missing"
                );
            }

            BigDecimal subtotal =
                    cartItem
                            .getUnitPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            cartItem
                                                    .getQuantity()
                                    )
                            );

            total =
                    total.add(
                            subtotal
                    );
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
                                        .equals(
                                                menuItemId
                                        )
                )
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Menu item not found"
                        )
                );
    }
}
