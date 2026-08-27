package com.foodordering.payment;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService
    ) {
        this.paymentService = paymentService;
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
}
