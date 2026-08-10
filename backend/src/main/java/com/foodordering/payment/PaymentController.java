package com.foodordering.payment;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService
    ) {
        this.paymentService =
                paymentService;
    }

    @PostMapping("/mpesa/callback")
    public ResponseEntity<Map<String, String>>
    handleMpesaCallback(
            @RequestBody
            MpesaCallbackRequest request
    ) {

        paymentService.handleMpesaCallback(
                request
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "M-Pesa callback received"
                )
        );
    }
}
