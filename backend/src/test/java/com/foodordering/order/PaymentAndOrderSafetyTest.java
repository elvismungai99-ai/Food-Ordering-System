package com.foodordering.order;

import com.foodordering.User.entity.User;
import com.foodordering.User.repository.UserRepository;
import com.foodordering.audit.AuditLogService;
import com.foodordering.cart.Cart;
import com.foodordering.cart.CartItem;
import com.foodordering.cart.CartRepository;
import com.foodordering.common.exception.BusinessRuleException;
import com.foodordering.menu.MenuItem;
import com.foodordering.menu.MenuItemRepository;
import com.foodordering.order.dto.CancelOrderRequest;
import com.foodordering.order.dto.OrderDto;
import com.foodordering.order.dto.PlaceOrderRequest;
import com.foodordering.payment.*;
import com.foodordering.restaurant.Restaurant;
import com.foodordering.restaurant.RestaurantRepository;
import com.foodordering.restaurant.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentAndOrderSafetyTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    private PricingService pricingService;
    private OrderStateMachine orderStateMachine;
    private OrderService orderService;

    private UUID customerId;
    private UUID restaurantId;
    private UUID ownerId;
    private MenuItem menuItem;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService(
                BigDecimal.valueOf(150),
                BigDecimal.valueOf(35),
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
        orderStateMachine = new OrderStateMachine();
        orderService = new OrderService(
                orderRepository,
                cartRepository,
                menuItemRepository,
                restaurantRepository,
                restaurantService,
                paymentService,
                pricingService,
                orderStateMachine
        );

        customerId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();
        ownerId = UUID.randomUUID();

        restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setName("Savory Grill");
        restaurant.setOwnerId(ownerId);

        menuItem = new MenuItem();
        menuItem.setId(UUID.randomUUID());
        menuItem.setName("Burger");
        menuItem.setPrice(BigDecimal.valueOf(600)); // True database price KES 600
        menuItem.setRestaurant(restaurant);
        menuItem.setAvailable(true);
    }

    @Test
    void testServerSidePricing_CalculatesOrderFromDatabasePrices() {
        // Customer's cart has 2 burgers
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setCustomerId(customerId);

        CartItem item = new CartItem();
        item.setId(UUID.randomUUID());
        item.setMenuItemId(menuItem.getId());
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(600));
        cart.addItem(item);

        when(cartRepository.findWithItemsByCustomerId(customerId)).thenReturn(Optional.of(cart));
        when(menuItemRepository.findById(menuItem.getId())).thenReturn(Optional.of(menuItem));
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantService.isApprovedAndOpen(restaurant)).thenReturn(true);
        when(paymentService.processPayment(any(), any(), any(), any()))
                .thenReturn(new PaymentResult(true, "COD-123", "Accepted", PaymentStatus.PENDING));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setDeliveryAddress("123 Main St, Nairobi");
        request.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderDto created = orderService.placeOrder(customerId, request);

        // Subtotal = 2 * 600 = 1200; Delivery = 150; Service = 35 => Total = 1385.00
        assertEquals(new BigDecimal("1200.00"), created.getSubtotalAmount());
        assertEquals(new BigDecimal("150.00"), created.getDeliveryFee());
        assertEquals(new BigDecimal("35.00"), created.getServiceFee());
        assertEquals(new BigDecimal("1385.00"), created.getTotalAmount());
    }

    @Test
    void testIdempotencyKey_ReturnsExistingOrderWithoutRecreation() {
        String idempotencyKey = "idemp-unique-token-abc-123";

        Order existingOrder = new Order();
        existingOrder.setId(UUID.randomUUID());
        existingOrder.setCustomerId(customerId);
        existingOrder.setIdempotencyKey(idempotencyKey);
        existingOrder.setStatus(OrderStatus.CONFIRMED);
        existingOrder.setPaymentStatus(PaymentStatus.PAID);
        existingOrder.setTotalAmount(BigDecimal.valueOf(1385));
        existingOrder.setSubtotalAmount(BigDecimal.valueOf(1200));

        when(orderRepository.findByCustomerIdAndIdempotencyKey(customerId, idempotencyKey))
                .thenReturn(Optional.of(existingOrder));

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setDeliveryAddress("123 Main St");
        request.setIdempotencyKey(idempotencyKey);

        OrderDto result = orderService.placeOrder(customerId, request);

        assertEquals(existingOrder.getId(), result.getId());
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        // Verify no new order was saved or payment charged
        verify(paymentService, never()).processPayment(any(), any(), any(), any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void testPaidOrderCannotRegressToPendingStatus() {
        Order paidOrder = new Order();
        paidOrder.setId(UUID.randomUUID());
        paidOrder.setRestaurantId(restaurantId);
        paidOrder.setStatus(OrderStatus.CONFIRMED);
        paidOrder.setPaymentStatus(PaymentStatus.PAID);

        when(orderRepository.findById(paidOrder.getId())).thenReturn(Optional.of(paidOrder));
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        assertThrows(BusinessRuleException.class, () ->
                orderService.updateOrderStatus(ownerId, paidOrder.getId(), OrderStatus.PENDING, false)
        );
    }

    @Test
    void testPaidOrderCancellation_TransitionsToRefundedWithReference() {
        Order paidOrder = new Order();
        paidOrder.setId(UUID.randomUUID());
        paidOrder.setCustomerId(customerId);
        paidOrder.setStatus(OrderStatus.PENDING);
        paidOrder.setPaymentStatus(PaymentStatus.PAID);

        when(orderRepository.findByIdAndCustomerId(paidOrder.getId(), customerId))
                .thenReturn(Optional.of(paidOrder));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDto cancelled = orderService.cancelCustomerOrder(customerId, paidOrder.getId(), "Changed my mind");

        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
        assertEquals(PaymentStatus.REFUNDED, cancelled.getPaymentStatus());
        assertNotNull(cancelled.getRefundReference());
        assertTrue(cancelled.getRefundReference().startsWith("REFUND-"));
        assertTrue(cancelled.getRefundReason().contains("Changed my mind"));
    }

    @Test
    void testAdminRefundOrder_Success() {
        Order paidOrder = new Order();
        paidOrder.setId(UUID.randomUUID());
        paidOrder.setStatus(OrderStatus.CONFIRMED);
        paidOrder.setPaymentStatus(PaymentStatus.PAID);

        when(orderRepository.findById(paidOrder.getId())).thenReturn(Optional.of(paidOrder));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UUID adminId = UUID.randomUUID();
        OrderDto refunded = orderService.refundOrder(paidOrder.getId(), "Customer requested refund", adminId);

        assertEquals(PaymentStatus.REFUNDED, refunded.getPaymentStatus());
        assertEquals(OrderStatus.CANCELLED, refunded.getStatus());
        assertNotNull(refunded.getRefundReference());
        assertEquals("Customer requested refund", refunded.getRefundReason());
        verify(paymentService).logRefundAudit(any(), eq(adminId));
    }
}

