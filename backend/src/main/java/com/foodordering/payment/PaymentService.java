package com.foodordering.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodordering.common.exception.BusinessRuleException;
import com.foodordering.order.Order;
import com.foodordering.order.OrderRepository;
import com.foodordering.order.PaymentStatus;

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

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final HttpClient httpClient =
            HttpClient.newHttpClient();

    private final boolean mpesaEnabled;
    private final String mpesaEnvironment;
    private final String mpesaConsumerKey;
    private final String mpesaConsumerSecret;
    private final String mpesaShortcode;
    private final String mpesaPasskey;
    private final String mpesaCallbackUrl;

    public PaymentService(
            ObjectMapper objectMapper,
            OrderRepository orderRepository,
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
            String mpesaCallbackUrl
    ) {
        this.objectMapper =
                objectMapper;
        this.orderRepository =
                orderRepository;
        this.mpesaEnabled =
                mpesaEnabled;
        this.mpesaEnvironment =
                mpesaEnvironment;
        this.mpesaConsumerKey =
                mpesaConsumerKey;
        this.mpesaConsumerSecret =
                mpesaConsumerSecret;
        this.mpesaShortcode =
                mpesaShortcode;
        this.mpesaPasskey =
                mpesaPasskey;
        this.mpesaCallbackUrl =
                mpesaCallbackUrl;
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

        if (
                amount == null
                || amount.compareTo(
                        BigDecimal.ZERO
                ) <= 0
        ) {
            return new PaymentResult(
                    false,
                    null,
                    "Invalid payment amount"
            );
        }

        PaymentMethod safeMethod =
                paymentMethod != null
                        ? paymentMethod
                        : PaymentMethod.CASH_ON_DELIVERY;

        return switch (safeMethod) {
            case MPESA ->
                    initiateMpesaPayment(
                            amount,
                            mpesaPhoneNumber
                    );
            case CASH_ON_DELIVERY ->
                    new PaymentResult(
                            true,
                            "COD-"
                            + UUID.randomUUID()
                                    .toString()
                                    .toUpperCase(),
                            "Cash on delivery order accepted",
                            PaymentStatus.PENDING
                    );
        };
    }

    @Transactional
    public void handleMpesaCallback(
            MpesaCallbackRequest request
    ) {

        MpesaCallbackRequest.StkCallback callback =
                request != null
                        ? request.getStkCallback()
                        : null;

        if (
                callback == null
                || callback.getCheckoutRequestId() == null
                || callback
                        .getCheckoutRequestId()
                        .isBlank()
        ) {
            throw new BusinessRuleException(
                    "Invalid M-Pesa callback payload"
            );
        }

        Order order =
                orderRepository
                        .findByPaymentReference(
                                callback
                                        .getCheckoutRequestId()
                        )
                        .orElseThrow(() ->
                                new BusinessRuleException(
                                        "Order not found for M-Pesa callback"
                                )
                        );

        order.setPaymentStatus(
                callback.isSuccessful()
                        ? PaymentStatus.PAID
                        : PaymentStatus.FAILED
        );

        orderRepository.save(
                order
        );
    }

    private PaymentResult initiateMpesaPayment(
            BigDecimal amount,
            String phoneNumber
    ) {

        requireConfigured(
                mpesaEnabled,
                "M-Pesa is not enabled. Set MPESA_ENABLED=true after adding Daraja credentials."
        );
        requireText(
                phoneNumber,
                "M-Pesa phone number is required"
        );
        requireText(mpesaConsumerKey, "MPESA_CONSUMER_KEY is required");
        requireText(mpesaConsumerSecret, "MPESA_CONSUMER_SECRET is required");
        requireText(mpesaShortcode, "MPESA_SHORTCODE is required");
        requireText(mpesaPasskey, "MPESA_PASSKEY is required");
        requireText(mpesaCallbackUrl, "MPESA_CALLBACK_URL is required");

        try {
            String accessToken =
                    getMpesaAccessToken();

            String timestamp =
                    LocalDateTime.now()
                            .format(
                                    DateTimeFormatter.ofPattern(
                                            "yyyyMMddHHmmss"
                                    )
                            );

            String password =
                    Base64.getEncoder()
                            .encodeToString(
                                    (
                                            mpesaShortcode
                                            + mpesaPasskey
                                            + timestamp
                                    ).getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            );

            Map<String, Object> payload =
                    Map.ofEntries(
                            entry("BusinessShortCode", mpesaShortcode),
                            entry("Password", password),
                            entry("Timestamp", timestamp),
                            entry("TransactionType", "CustomerPayBillOnline"),
                            entry("Amount", amount
                                    .setScale(
                                            0,
                                            RoundingMode.HALF_UP
                                    )
                                    .intValue()),
                            entry("PartyA", normalizeKenyanPhone(phoneNumber)),
                            entry("PartyB", mpesaShortcode),
                            entry("PhoneNumber", normalizeKenyanPhone(phoneNumber)),
                            entry("CallBackURL", mpesaCallbackUrl),
                            entry("AccountReference", "FOOD-ORDER"),
                            entry("TransactionDesc", "Food order payment")
                    );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            mpesaBaseUrl()
                                            + "/mpesa/stkpush/v1/processrequest"
                                    )
                            )
                            .header(
                                    "Authorization",
                                    "Bearer " + accessToken
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            objectMapper.writeValueAsString(
                                                    payload
                                            )
                                    )
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (
                    response.statusCode() < 200
                    || response.statusCode() >= 300
            ) {
                return new PaymentResult(
                        false,
                        null,
                        "M-Pesa payment initiation failed: "
                        + response.body()
                );
            }

            JsonNode json =
                    objectMapper.readTree(
                            response.body()
                    );

            String reference =
                    json.path("CheckoutRequestID")
                            .asText(
                                    "MPESA-"
                                    + UUID.randomUUID()
                                            .toString()
                                            .toUpperCase()
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
                    "M-Pesa payment initiation failed: "
                    + exception.getMessage()
            );
        }
    }

    private String getMpesaAccessToken()
            throws Exception {

        String credentials =
                Base64.getEncoder()
                        .encodeToString(
                                (
                                        mpesaConsumerKey
                                        + ":"
                                        + mpesaConsumerSecret
                                ).getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        mpesaBaseUrl()
                                        + "/oauth/v1/generate?grant_type=client_credentials"
                                )
                        )
                        .header(
                                "Authorization",
                                "Basic " + credentials
                        )
                        .GET()
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (
                response.statusCode() < 200
                || response.statusCode() >= 300
        ) {
            throw new BusinessRuleException(
                    "Unable to get M-Pesa access token"
            );
        }

        return objectMapper
                .readTree(response.body())
                .path("access_token")
                .asText();
    }

    private String mpesaBaseUrl() {
        return "production".equalsIgnoreCase(
                mpesaEnvironment
        )
                ? "https://api.safaricom.co.ke"
                : "https://sandbox.safaricom.co.ke";
    }

    private String normalizeKenyanPhone(
            String phoneNumber
    ) {

        String normalized =
                phoneNumber
                        .trim()
                        .replaceAll("\\s+", "")
                        .replace("-", "");

        if (normalized.startsWith("+")) {
            normalized =
                    normalized.substring(1);
        }

        if (normalized.startsWith("0")) {
            normalized =
                    "254" + normalized.substring(1);
        }

        return normalized;
    }

    private void requireConfigured(
            boolean enabled,
            String message
    ) {

        if (!enabled) {
            throw new BusinessRuleException(
                    message
            );
        }
    }

    private void requireText(
            String value,
            String message
    ) {

        if (value == null || value.isBlank()) {
            throw new BusinessRuleException(
                    message
            );
        }
    }
}
