package com.foodordering.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodordering.User.entity.User;
import com.foodordering.User.repository.UserRepository;
import com.foodordering.common.exception.BusinessRuleException;
import com.foodordering.common.exception.ForbiddenOperationException;
import com.foodordering.common.exception.ResourceNotFoundException;
import com.foodordering.order.Order;
import com.foodordering.order.OrderRepository;
import com.foodordering.order.OrderStatus;
import com.foodordering.order.PaymentStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCallbackSecurityTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    private ObjectMapper objectMapper;
    private PaymentService paymentService;

    private final String testSecret = "secure-webhook-secret-xyz";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        paymentService = new PaymentService(
                objectMapper,
                orderRepository,
                userRepository,
                false, // mpesaEnabled (mocked in tests)
                "sandbox",
                "",
                "",
                "",
                "",
                "https://api.example.com/api/payments/mpesa/callback",
                testSecret
        );
    }

    private Order createTestOrder(UUID orderId, UUID customerId, String checkoutId, BigDecimal totalAmount) {
        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(customerId);
        order.setRestaurantId(UUID.randomUUID());
        order.setRestaurantName("Pizza Palace");
        order.setDeliveryAddress("Nairobi CBD, Kimathi Street");
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setPaymentReference(checkoutId);
        order.setPaymentMethod(PaymentMethod.MPESA);
        order.setTotalAmount(totalAmount);
        order.setSubtotalAmount(totalAmount);
        order.setDeliveryFee(BigDecimal.ZERO);
        order.setServiceFee(BigDecimal.ZERO);
        return order;
    }

    private MpesaCallbackRequest createSuccessCallback(String checkoutId, BigDecimal amount, String phone, String receipt) {
        MpesaCallbackRequest request = new MpesaCallbackRequest();
        MpesaCallbackRequest.Body body = new MpesaCallbackRequest.Body();
        MpesaCallbackRequest.StkCallback callback = new MpesaCallbackRequest.StkCallback();

        callback.setCheckoutRequestId(checkoutId);
        callback.setResultCode(0);
        callback.setResultDescription("The service request is processed successfully.");

        MpesaCallbackRequest.CallbackMetadata metadata = new MpesaCallbackRequest.CallbackMetadata();
        MpesaCallbackRequest.Item itemAmount = new MpesaCallbackRequest.Item();
        itemAmount.setName("Amount");
        itemAmount.setValue(amount.doubleValue());

        MpesaCallbackRequest.Item itemReceipt = new MpesaCallbackRequest.Item();
        itemReceipt.setName("MpesaReceiptNumber");
        itemReceipt.setValue(receipt);

        MpesaCallbackRequest.Item itemPhone = new MpesaCallbackRequest.Item();
        itemPhone.setName("PhoneNumber");
        itemPhone.setValue(phone);

        metadata.setItem(List.of(itemAmount, itemReceipt, itemPhone));
        callback.setCallbackMetadata(metadata);
        body.setStkCallback(callback);
        request.setBody(body);

        return request;
    }

    @Test
    void testValidCallback_MarksOrderAsPaidAndConfirmed() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String checkoutId = "ws_CO_001";
        BigDecimal amount = new BigDecimal("850.00");

        Order order = createTestOrder(orderId, customerId, checkoutId, amount);

        User customer = new User();
        customer.setId(customerId);
        customer.setPhoneNumber("0712345678");

        when(orderRepository.findByPaymentReference(checkoutId)).thenReturn(Optional.of(order));
        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MpesaCallbackRequest callbackRequest = createSuccessCallback(checkoutId, amount, "254712345678", "NLJ7RT61SV");

        PaymentCallbackResult result = paymentService.handleMpesaCallback(callbackRequest, testSecret);

        assertTrue(result.isSuccessful());
        assertEquals("PAID", result.getStatus());
        assertFalse(result.isIdempotent());
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertTrue(order.getPaymentReference().contains("NLJ7RT61SV"));

        verify(orderRepository).save(order);
    }

    @Test
    void testIdempotentCallback_DoesNotReProcess() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String checkoutId = "ws_CO_002";
        BigDecimal amount = new BigDecimal("500.00");

        Order order = createTestOrder(orderId, customerId, checkoutId, amount);
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findByPaymentReference(checkoutId)).thenReturn(Optional.of(order));

        MpesaCallbackRequest callbackRequest = createSuccessCallback(checkoutId, amount, "254712345678", "NLJ7RT61SV");

        PaymentCallbackResult result = paymentService.handleMpesaCallback(callbackRequest, testSecret);

        assertTrue(result.isSuccessful());
        assertTrue(result.isIdempotent());
        assertEquals("PAID", result.getStatus());

        // Verify save was not called since order was already paid
        verify(orderRepository, never()).save(order);
    }

    @Test
    void testAmountMismatch_NeverMarksAsPaid_MarksAsFailed() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String checkoutId = "ws_CO_003";
        BigDecimal expectedAmount = new BigDecimal("1200.00");
        BigDecimal paidAmount = new BigDecimal("200.00"); // Short payment

        Order order = createTestOrder(orderId, customerId, checkoutId, expectedAmount);

        when(orderRepository.findByPaymentReference(checkoutId)).thenReturn(Optional.of(order));

        MpesaCallbackRequest callbackRequest = createSuccessCallback(checkoutId, paidAmount, "254712345678", "NLJ7RT61SV");

        assertThrows(BusinessRuleException.class, () ->
                paymentService.handleMpesaCallback(callbackRequest, testSecret)
        );

        assertEquals(PaymentStatus.FAILED, order.getPaymentStatus());
        assertNotEquals(PaymentStatus.PAID, order.getPaymentStatus());
        assertTrue(order.getCancellationReason().contains("Security mismatch"));
        verify(orderRepository).save(order);
    }

    @Test
    void testPhoneMismatch_NeverMarksAsPaid_MarksAsFailed() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String checkoutId = "ws_CO_004";
        BigDecimal amount = new BigDecimal("750.00");

        Order order = createTestOrder(orderId, customerId, checkoutId, amount);

        User customer = new User();
        customer.setId(customerId);
        customer.setPhoneNumber("0712345678");

        when(orderRepository.findByPaymentReference(checkoutId)).thenReturn(Optional.of(order));
        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // Callback sent from a different phone number
        MpesaCallbackRequest callbackRequest = createSuccessCallback(checkoutId, amount, "254799999999", "NLJ7RT61SV");

        assertThrows(BusinessRuleException.class, () ->
                paymentService.handleMpesaCallback(callbackRequest, testSecret)
        );

        assertEquals(PaymentStatus.FAILED, order.getPaymentStatus());
        assertNotEquals(PaymentStatus.PAID, order.getPaymentStatus());
        assertTrue(order.getCancellationReason().contains("phone"));
        verify(orderRepository).save(order);
    }

    @Test
    void testFailedTransaction_MarksAsFailedWithReason() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String checkoutId = "ws_CO_005";
        BigDecimal amount = new BigDecimal("450.00");

        Order order = createTestOrder(orderId, customerId, checkoutId, amount);

        when(orderRepository.findByPaymentReference(checkoutId)).thenReturn(Optional.of(order));

        MpesaCallbackRequest request = new MpesaCallbackRequest();
        MpesaCallbackRequest.Body body = new MpesaCallbackRequest.Body();
        MpesaCallbackRequest.StkCallback callback = new MpesaCallbackRequest.StkCallback();
        callback.setCheckoutRequestId(checkoutId);
        callback.setResultCode(1032);
        callback.setResultDescription("Request cancelled by user");
        body.setStkCallback(callback);
        request.setBody(body);

        PaymentCallbackResult result = paymentService.handleMpesaCallback(request, testSecret);

        assertFalse(result.isSuccessful());
        assertEquals("FAILED", result.getStatus());
        assertEquals(PaymentStatus.FAILED, order.getPaymentStatus());
        assertTrue(order.getCancellationReason().contains("cancelled by user"));
        verify(orderRepository).save(order);
    }

    @Test
    void testInvalidSecret_RejectsWithForbidden() {
        MpesaCallbackRequest request = new MpesaCallbackRequest();

        assertThrows(ForbiddenOperationException.class, () ->
                paymentService.handleMpesaCallback(request, "wrong-secret")
        );

        assertThrows(ForbiddenOperationException.class, () ->
                paymentService.handleMpesaCallback(request, null)
        );
    }

    @Test
    void testUnknownCheckoutId_ThrowsResourceNotFound() {
        String checkoutId = "ws_CO_UNKNOWN";
        when(orderRepository.findByPaymentReference(checkoutId)).thenReturn(Optional.empty());
        when(orderRepository.findFirstByPaymentReferenceStartingWith(checkoutId)).thenReturn(Optional.empty());

        MpesaCallbackRequest callbackRequest = createSuccessCallback(checkoutId, new BigDecimal("100"), "254712345678", "REC123");

        assertThrows(ResourceNotFoundException.class, () ->
                paymentService.handleMpesaCallback(callbackRequest, testSecret)
        );
    }

    @Test
    void testDuplicateMpesaReceiptClaimedByAnotherOrder_IsRejected() {
        UUID orderIdA = UUID.randomUUID();
        UUID orderIdB = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String checkoutIdB = "ws_CO_006";
        String stolenReceipt = "NLJ7RT61SV";
        BigDecimal amount = new BigDecimal("500.00");

        Order orderA = createTestOrder(orderIdA, customerId, "ws_CO_PREV", amount);
        orderA.setProviderTransactionId(stolenReceipt);

        Order orderB = createTestOrder(orderIdB, customerId, checkoutIdB, amount);

        when(orderRepository.findByPaymentReference(checkoutIdB)).thenReturn(Optional.of(orderB));
        when(orderRepository.findByProviderTransactionId(stolenReceipt)).thenReturn(Optional.of(orderA));

        MpesaCallbackRequest callbackRequest = createSuccessCallback(checkoutIdB, amount, "254712345678", stolenReceipt);

        assertThrows(BusinessRuleException.class, () ->
                paymentService.handleMpesaCallback(callbackRequest, testSecret)
        );
    }
}

