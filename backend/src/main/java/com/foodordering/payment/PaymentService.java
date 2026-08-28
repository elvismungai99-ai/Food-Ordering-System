package com.foodordering.payment;

import com.fasterxml.jackson.databind.JsonNode;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import static java.util.Map.entry;

@Service
public class PaymentService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final HttpClient httpClient =
            HttpClient.newHttpClient();

    private final boolean mpesaEnabled;
    private final String mpesaEnvironment;
    private final String mpesaConsumerKey;
    private final String mpesaConsumerSecret;
    private final String mpesaShortcode;
    private final String mpesaPasskey;
    private final String mpesaCallbackUrl;
    private final String mpesaCallbackSecret;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.foodordering.audit.AuditLogService auditLogService;

    public PaymentService(
            ObjectMapper objectMapper,
            OrderRepository orderRepository,
            UserRepository userRepository,
            @Value("${payments.mpesa.enabled:false}")
            boolean mpesaEnabled,
            @Value("${payments.mpesa.environment:sandbox}")
            String mpesaEnvironment,
            @Value("${payments.mpesa.consumer-key:}")
            String mpesaConsumerKey,
            @Value("${payments.mpesa.consumer-secret:}")
            String mpesaConsumerSecret,
            @Value("${payments.mpesa.shortcode:}")
            String mpesaShortcode,
            @Value("${payments.mpesa.passkey:}")
            String mpesaPasskey,
            @Value("${payments.mpesa.callback-url:}")
            String mpesaCallbackUrl,
            @Value("${payments.mpesa.callback-secret:}")
            String mpesaCallbackSecret
    ) {
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.mpesaEnabled = mpesaEnabled;
        this.mpesaEnvironment = mpesaEnvironment;
        this.mpesaConsumerKey = mpesaConsumerKey;
        this.mpesaConsumerSecret = mpesaConsumerSecret;
        this.mpesaShortcode = mpesaShortcode;
        this.mpesaPasskey = mpesaPasskey;
        this.mpesaCallbackUrl = mpesaCallbackUrl;
        this.mpesaCallbackSecret = mpesaCallbackSecret;
    }

    public PaymentResult processPayment(
            UUID customerId,
            BigDecimal amount
    ) {
        return processPayment(
                customerId,
                amount,
                PaymentMethod.CASH_ON_DELIVERY,
                null
        );
    }

    public PaymentResult processPayment(
            UUID customerId,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            String mpesaPhoneNumber
    ) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new PaymentResult(
                    false,
                    null,
                    "Invalid payment amount"
            );
        }

        PaymentMethod safeMethod = paymentMethod != null
                ? paymentMethod
                : PaymentMethod.CASH_ON_DELIVERY;

        return switch (safeMethod) {
            case MPESA -> initiateMpesaPayment(
                    amount,
                    mpesaPhoneNumber
            );
            case CASH_ON_DELIVERY -> new PaymentResult(
                    true,
                    "COD-" + UUID.randomUUID().toString().toUpperCase(),
                    "Cash on delivery order accepted",
                    PaymentStatus.PENDING
            );
        };
    }

    @Transactional
    public PaymentCallbackResult handleMpesaCallback(
            MpesaCallbackRequest request
    ) {
        return handleMpesaCallback(request, null);
    }

    @Transactional
    public PaymentCallbackResult handleMpesaCallback(
            MpesaCallbackRequest request,
            String providedSecret
    ) {
        // 1. Verify Callback Authenticity via Secret Token (if configured)
        verifyCallbackSecret(providedSecret);

        // 2. Validate Payload Structure
        MpesaCallbackRequest.StkCallback callback = request != null
                ? request.getStkCallback()
                : null;

        if (callback == null || callback.getCheckoutRequestId() == null || callback.getCheckoutRequestId().isBlank()) {
            log.error("Rejecting M-Pesa callback: Missing or empty CheckoutRequestID in payload.");
            throw new BusinessRuleException("Invalid M-Pesa callback payload: Missing CheckoutRequestID");
        }

        String checkoutRequestId = callback.getCheckoutRequestId().trim();

        // 3. Look Up Matching Order by Checkout ID / Payment Reference
        Order order = orderRepository.findByPaymentReference(checkoutRequestId)
                .or(() -> orderRepository.findFirstByPaymentReferenceStartingWith(checkoutRequestId))
                .orElseThrow(() -> {
                    log.error("Rejecting M-Pesa callback: No order found matching checkout ID {}", checkoutRequestId);
                    return new ResourceNotFoundException("No order found matching checkout ID: " + checkoutRequestId);
                });

        // 4. Idempotency Check: Don't re-process already completed payments
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            log.info("Idempotent M-Pesa callback: Order {} is already marked as PAID for checkout ID {}.",
                    order.getId(), checkoutRequestId);
            return new PaymentCallbackResult(true, "PAID", "Order is already marked as paid (idempotent)", true);
        }

        if (order.getPaymentStatus() == PaymentStatus.FAILED && !callback.isSuccessful()) {
            log.info("Idempotent M-Pesa callback: Order {} is already marked as FAILED for checkout ID {}.",
                    order.getId(), checkoutRequestId);
            return new PaymentCallbackResult(false, "FAILED", "Order is already marked as failed (idempotent)", true);
        }

        // 5. Validate Transaction Status
        if (!callback.isSuccessful()) {
            String failureDescription = callback.getResultDescription() != null && !callback.getResultDescription().isBlank()
                    ? callback.getResultDescription().trim()
                    : "M-Pesa transaction rejected (ResultCode: " + callback.getResultCode() + ")";

            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setCancellationReason("M-Pesa payment failed: " + failureDescription);
            orderRepository.save(order);

            log.warn("M-Pesa transaction failed for order {} (checkout ID: {}): {}",
                    order.getId(), checkoutRequestId, failureDescription);
            return new PaymentCallbackResult(false, "FAILED", failureDescription, false);
        }

        // 6. Validate Payment Amount
        BigDecimal callbackAmount = callback.getAmount();
        if (callbackAmount != null) {
            BigDecimal expectedAmount = order.getTotalAmount();
            if (expectedAmount != null && callbackAmount.compareTo(expectedAmount) < 0) {
                log.error("SECURITY ALERT: Amount mismatch on order {}! Expected KES {}, but callback reported KES {}.",
                        order.getId(), expectedAmount, callbackAmount);

                order.setPaymentStatus(PaymentStatus.FAILED);
                order.setCancellationReason("Security mismatch: Paid amount KES " + callbackAmount
                        + " is less than order total KES " + expectedAmount);
                orderRepository.save(order);

                throw new BusinessRuleException("Payment amount mismatch: Expected KES " + expectedAmount
                        + ", received KES " + callbackAmount);
            }
        }

        // 7. Validate Customer Phone Number (if present in callback metadata and customer profile)
        String callbackPhone = callback.getPhoneNumber();
        if (callbackPhone != null && !callbackPhone.isBlank()) {
            User customer = userRepository.findById(order.getCustomerId()).orElse(null);
            if (customer != null && customer.getPhoneNumber() != null && !customer.getPhoneNumber().isBlank()) {
                String normalizedCustomerPhone = normalizeKenyanPhone(customer.getPhoneNumber());
                String normalizedCallbackPhone = normalizeKenyanPhone(callbackPhone);

                if (!normalizedCustomerPhone.equals(normalizedCallbackPhone)) {
                    log.error("SECURITY ALERT: Phone mismatch on order {}! Customer registered phone {} vs callback phone {}.",
                            order.getId(), normalizedCustomerPhone, normalizedCallbackPhone);

                    order.setPaymentStatus(PaymentStatus.FAILED);
                    order.setCancellationReason("Security mismatch: Payment received from phone " + normalizedCallbackPhone
                            + " which does not match customer phone " + normalizedCustomerPhone);
                    orderRepository.save(order);

                    throw new BusinessRuleException("Payment phone number mismatch");
                }
            }
        }

        // 8. Direct Daraja Query Validation (if active in production/sandbox)
        if (mpesaEnabled && isDarajaConfigured()) {
            verifyDirectlyWithDaraja(checkoutRequestId, order);
        }

        // 9. All Validations Passed -> Mark Order as PAID & CONFIRMED
        order.setPaymentStatus(PaymentStatus.PAID);
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
        }

        String receipt = callback.getMpesaReceiptNumber();
        if (receipt != null && !receipt.isBlank()) {
            String cleanReceipt = receipt.trim();
            var existingWithReceipt = orderRepository.findByProviderTransactionId(cleanReceipt);
            if (existingWithReceipt.isPresent() && !existingWithReceipt.get().getId().equals(order.getId())) {
                log.error("SECURITY ALERT: Duplicate payment receipt {}! Already assigned to order {}",
                        cleanReceipt, existingWithReceipt.get().getId());
                throw new BusinessRuleException("Payment receipt has already been processed for another order");
            }
            order.setProviderTransactionId(cleanReceipt);
            order.setPaymentReference(checkoutRequestId + "|" + cleanReceipt);
        }

        orderRepository.save(order);
        if (auditLogService != null) {
            auditLogService.logAction("PAYMENT_COMPLETED", order.getId().toString(), "ORDER", "Payment verified via M-Pesa. Receipt: " + receipt);
        }
        log.info("Order {} successfully verified and marked as PAID. M-Pesa Receipt: {}", order.getId(), receipt);

        return new PaymentCallbackResult(true, "PAID", "Payment verified and order marked as paid", false);
    }

    public void logRefundAudit(Order order, UUID actorId) {
        if (auditLogService != null) {
            auditLogService.logAction(
                    "ORDER_REFUNDED",
                    order.getId().toString(),
                    "ORDER",
                    "Refund issued: " + order.getRefundReason() + ". Ref: " + order.getRefundReference()
            );
        }
        log.info("Order {} refunded. Reason: {}. Reference: {}",
                order.getId(), order.getRefundReason(), order.getRefundReference());
    }

    @Transactional
    public PaymentResult reconcilePayment(UUID orderId, UUID actorId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return new PaymentResult(true, order.getPaymentReference(), "Order is already paid", PaymentStatus.PAID);
        }

        if (order.getPaymentMethod() != PaymentMethod.MPESA) {
            return new PaymentResult(true, order.getPaymentReference(), "Payment method " + order.getPaymentMethod() + " status is " + order.getPaymentStatus(), order.getPaymentStatus());
        }

        if (order.getPaymentReference() == null || order.getPaymentReference().isBlank()) {
            throw new BusinessRuleException("Order does not have a checkout reference for reconciliation");
        }

        String checkoutRequestId = order.getPaymentReference().split("\\|")[0].trim();

        if (mpesaEnabled && isDarajaConfigured()) {
            try {
                JsonNode statusResponse = queryMpesaTransactionStatus(checkoutRequestId);
                if (statusResponse != null) {
                    int resultCode = statusResponse.path("ResultCode").asInt(-1);
                    if (resultCode == 0) {
                        order.setPaymentStatus(PaymentStatus.PAID);
                        if (order.getStatus() == OrderStatus.PENDING) {
                            order.setStatus(OrderStatus.CONFIRMED);
                        }
                        orderRepository.save(order);
                        if (auditLogService != null) {
                            auditLogService.logAction("PAYMENT_RECONCILED", order.getId().toString(), "ORDER", "Payment verified and reconciled via Daraja query");
                        }
                        return new PaymentResult(true, order.getPaymentReference(), "Payment verified and reconciled as PAID", PaymentStatus.PAID);
                    } else {
                        String resultDesc = statusResponse.path("ResultDesc").asText("Transaction unsuccessful");
                        order.setPaymentStatus(PaymentStatus.FAILED);
                        order.setCancellationReason("Reconciliation: " + resultDesc);
                        orderRepository.save(order);
                        return new PaymentResult(false, order.getPaymentReference(), "Payment failed: " + resultDesc, PaymentStatus.FAILED);
                    }
                }
            } catch (Exception e) {
                log.warn("Direct Daraja reconciliation query failed for order {}: {}", orderId, e.getMessage());
            }
        }

        return new PaymentResult(false, order.getPaymentReference(), "Reconciliation completed. Current status: " + order.getPaymentStatus(), order.getPaymentStatus());
    }

    private void verifyCallbackSecret(String providedSecret) {
        if (mpesaCallbackSecret != null && !mpesaCallbackSecret.isBlank()) {
            if (providedSecret == null || !mpesaCallbackSecret.trim().equals(providedSecret.trim())) {
                log.warn("SECURITY ALERT: Unauthorized M-Pesa callback rejected. Invalid or missing secret token.");
                throw new ForbiddenOperationException("Unauthorized M-Pesa callback: Invalid verification secret");
            }
        }
    }

    private boolean isDarajaConfigured() {
        return mpesaConsumerKey != null && !mpesaConsumerKey.isBlank()
                && mpesaConsumerSecret != null && !mpesaConsumerSecret.isBlank()
                && mpesaShortcode != null && !mpesaShortcode.isBlank()
                && mpesaPasskey != null && !mpesaPasskey.isBlank();
    }

    private void verifyDirectlyWithDaraja(String checkoutRequestId, Order order) {
        try {
            JsonNode statusResponse = queryMpesaTransactionStatus(checkoutRequestId);
            if (statusResponse != null) {
                int resultCode = statusResponse.path("ResultCode").asInt(-1);
                if (resultCode != 0) {
                    String resultDesc = statusResponse.path("ResultDesc").asText("Daraja status query reported failure");
                    log.error("Daraja status query for checkout ID {} returned failure code {}: {}",
                            checkoutRequestId, resultCode, resultDesc);

                    order.setPaymentStatus(PaymentStatus.FAILED);
                    order.setCancellationReason("Daraja direct verification failed: " + resultDesc);
                    orderRepository.save(order);
                    throw new BusinessRuleException("Daraja payment verification failed: " + resultDesc);
                }
            }
        } catch (BusinessRuleException bre) {
            throw bre;
        } catch (Exception e) {
            log.warn("Could not query Daraja directly for checkout ID {} (falling back to verified webhook): {}",
                    checkoutRequestId, e.getMessage());
        }
    }

    public JsonNode queryMpesaTransactionStatus(String checkoutRequestId) {
        if (!mpesaEnabled || !isDarajaConfigured()) {
            return null;
        }

        try {
            String accessToken = getMpesaAccessToken();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String password = Base64.getEncoder().encodeToString(
                    (mpesaShortcode + mpesaPasskey + timestamp).getBytes(StandardCharsets.UTF_8)
            );

            Map<String, Object> payload = Map.of(
                    "BusinessShortCode", mpesaShortcode,
                    "Password", password,
                    "Timestamp", timestamp,
                    "CheckoutRequestID", checkoutRequestId
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(mpesaBaseUrl() + "/mpesa/stkpushquery/v1/query"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readTree(response.body());
            }
        } catch (Exception e) {
            log.warn("Direct Daraja STK status query failed for {}: {}", checkoutRequestId, e.getMessage());
        }
        return null;
    }

    private PaymentResult initiateMpesaPayment(
            BigDecimal amount,
            String phoneNumber
    ) {
        requireConfigured(
                mpesaEnabled,
                "M-Pesa is not enabled. Set MPESA_ENABLED=true after adding Daraja credentials."
        );
        requireText(phoneNumber, "M-Pesa phone number is required");
        requireText(mpesaConsumerKey, "MPESA_CONSUMER_KEY is required");
        requireText(mpesaConsumerSecret, "MPESA_CONSUMER_SECRET is required");
        requireText(mpesaShortcode, "MPESA_SHORTCODE is required");
        requireText(mpesaPasskey, "MPESA_PASSKEY is required");
        requireText(mpesaCallbackUrl, "MPESA_CALLBACK_URL is required");

        try {
            String accessToken = getMpesaAccessToken();

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            String password = Base64.getEncoder().encodeToString(
                    (mpesaShortcode + mpesaPasskey + timestamp).getBytes(StandardCharsets.UTF_8)
            );

            String finalCallbackUrl = mpesaCallbackUrl;
            if (mpesaCallbackSecret != null && !mpesaCallbackSecret.isBlank()) {
                finalCallbackUrl += (mpesaCallbackUrl.contains("?") ? "&" : "?") + "secret=" + mpesaCallbackSecret.trim();
            }

            Map<String, Object> payload = Map.ofEntries(
                    entry("BusinessShortCode", mpesaShortcode),
                    entry("Password", password),
                    entry("Timestamp", timestamp),
                    entry("TransactionType", "CustomerPayBillOnline"),
                    entry("Amount", amount.setScale(0, RoundingMode.HALF_UP).intValue()),
                    entry("PartyA", normalizeKenyanPhone(phoneNumber)),
                    entry("PartyB", mpesaShortcode),
                    entry("PhoneNumber", normalizeKenyanPhone(phoneNumber)),
                    entry("CallBackURL", finalCallbackUrl),
                    entry("AccountReference", "FOOD-ORDER"),
                    entry("TransactionDesc", "Food order payment")
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(mpesaBaseUrl() + "/mpesa/stkpush/v1/processrequest"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return new PaymentResult(
                        false,
                        null,
                        "M-Pesa payment initiation failed: " + response.body()
                );
            }

            JsonNode json = objectMapper.readTree(response.body());
            String reference = json.path("CheckoutRequestID").asText(
                    "MPESA-" + UUID.randomUUID().toString().toUpperCase()
            );

            return new PaymentResult(
                    true,
                    reference,
                    "M-Pesa STK push sent. Await callback confirmation.",
                    PaymentStatus.PENDING
            );
        } catch (Exception exception) {
            return new PaymentResult(
                    false,
                    null,
                    "M-Pesa payment initiation failed: " + exception.getMessage()
            );
        }
    }

    private String getMpesaAccessToken() throws Exception {
        String credentials = Base64.getEncoder().encodeToString(
                (mpesaConsumerKey + ":" + mpesaConsumerSecret).getBytes(StandardCharsets.UTF_8)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mpesaBaseUrl() + "/oauth/v1/generate?grant_type=client_credentials"))
                .header("Authorization", "Basic " + credentials)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new BusinessRuleException("Unable to get M-Pesa access token");
        }

        return objectMapper.readTree(response.body()).path("access_token").asText();
    }

    private String mpesaBaseUrl() {
        return "production".equalsIgnoreCase(mpesaEnvironment)
                ? "https://api.safaricom.co.ke"
                : "https://sandbox.safaricom.co.ke";
    }

    public String normalizeKenyanPhone(String phoneNumber) {
        if (phoneNumber == null) return "";
        String normalized = phoneNumber.trim().replaceAll("\\s+", "").replace("-", "");

        if (normalized.startsWith("+")) {
            normalized = normalized.substring(1);
        }

        if (normalized.startsWith("0")) {
            normalized = "254" + normalized.substring(1);
        }

        return normalized;
    }

    private void requireConfigured(boolean enabled, String message) {
        if (!enabled) {
            throw new BusinessRuleException(message);
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException(message);
        }
    }
}
