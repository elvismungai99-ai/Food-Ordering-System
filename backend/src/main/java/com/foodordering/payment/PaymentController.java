package com.foodordering.payment;

import java.util.Map;
import java.util.UUID;

import com.foodordering.User.entity.User;
import com.foodordering.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final SecurityUtils securityUtils;

    @Autowired
    public PaymentController(
            PaymentService paymentService,
            @Autowired(required = false) SecurityUtils securityUtils
    ) {
        this.paymentService = paymentService;
        this.securityUtils = securityUtils;
    }

    public PaymentController(PaymentService paymentService) {
        this(paymentService, null);
    }

    @PostMapping("/mpesa/callback")
    public ResponseEntity<Map<String, Object>> handleMpesaCallback(
            @RequestHeader(value = "X-Callback-Secret", required = false)
            String secretHeader,

            @RequestParam(value = "secret", required = false)
            String secretParam,

            @RequestParam(value = "token", required = false)
            String tokenParam,

            @RequestBody
            MpesaCallbackRequest request
    ) {
        String providedSecret = secretHeader != null && !secretHeader.isBlank()
                ? secretHeader.trim()
                : secretParam != null && !secretParam.isBlank()
                ? secretParam.trim()
                : tokenParam != null ? tokenParam.trim() : null;

        PaymentCallbackResult result = paymentService.handleMpesaCallback(
                request,
                providedSecret
        );

        return ResponseEntity.ok(
                Map.of(
                        "ResultCode", 0,
                        "ResultDesc", "Callback processed successfully",
                        "status", result.getStatus(),
                        "message", result.getMessage(),
                        "idempotent", result.isIdempotent()
                )
        );
    }

    @PostMapping("/reconcile/{orderId}")
    public ResponseEntity<PaymentResult> reconcileOrderPayment(
            @PathVariable UUID orderId
    ) {
        UUID actorId = securityUtils != null ? securityUtils.getCurrentUserId() : null;
        return ResponseEntity.ok(paymentService.reconcilePayment(orderId, actorId));
    }

    @PostMapping("/simulate-callback/{orderId}")
    public ResponseEntity<PaymentResult> simulateCallback(
            @PathVariable UUID orderId,
            @RequestParam(value = "approve", defaultValue = "true") boolean approve
    ) {
        return ResponseEntity.ok(paymentService.simulateMpesaCallback(orderId, approve));
    }
}
