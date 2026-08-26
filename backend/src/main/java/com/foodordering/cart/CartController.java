package com.foodordering.cart;

import com.foodordering.cart.dto.AddCartItemRequest;
import com.foodordering.cart.dto.CartDto;
import com.foodordering.cart.dto.UpdateCartItemRequest;
import com.foodordering.security.JwtUtil;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import com.foodordering.common.exception.ForbiddenOperationException;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final JwtUtil jwtUtil;

    public CartController(
            CartService cartService,
            JwtUtil jwtUtil
    ) {
        this.cartService =
                cartService;

        this.jwtUtil =
                jwtUtil;
    }

    @GetMapping
    public ResponseEntity<CartDto> getCart(
            @RequestHeader("Authorization")
            String authHeader
    ) {

        UUID customerId =
                extractUserId(
                        authHeader
                );

        requireCustomer(
                authHeader
        );

        return ResponseEntity.ok(
                cartService.getCart(
                        customerId
                )
        );
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(

            @RequestHeader("Authorization")
            String authHeader,

            @Valid
            @RequestBody
            AddCartItemRequest request
    ) {

        UUID customerId =
                extractUserId(
                        authHeader
                );

        requireCustomer(
                authHeader
        );

        return ResponseEntity.ok(
                cartService.addItem(
                        customerId,
                        request
                )
        );
    }

    @PatchMapping(
            "/items/{cartItemId}"
    )
    public ResponseEntity<CartDto>
    updateQuantity(

            @RequestHeader("Authorization")
            String authHeader,

            @PathVariable
            UUID cartItemId,

            @Valid
            @RequestBody
            UpdateCartItemRequest request
    ) {

        UUID customerId =
                extractUserId(
                        authHeader
                );

        requireCustomer(
                authHeader
        );

        return ResponseEntity.ok(
                cartService.updateQuantity(
                        customerId,
                        cartItemId,
                        request.getQuantity()
                )
        );
    }

    @DeleteMapping(
            "/items/{cartItemId}"
    )
    public ResponseEntity<CartDto>
    removeItem(

            @RequestHeader("Authorization")
            String authHeader,

            @PathVariable
            UUID cartItemId
    ) {

        UUID customerId =
                extractUserId(
                        authHeader
                );

        requireCustomer(
                authHeader
        );

        return ResponseEntity.ok(
                cartService.removeItem(
                        customerId,
                        cartItemId
                )
        );
    }

    @PostMapping(
            "/accept-price-changes"
    )
    public ResponseEntity<CartDto>
    acceptPriceChanges(

            @RequestHeader("Authorization")
            String authHeader
    ) {

        UUID customerId =
                extractUserId(
                        authHeader
                );

        requireCustomer(
                authHeader
        );

        return ResponseEntity.ok(
                cartService
                        .acceptPriceChanges(
                                customerId
                        )
        );
    }

    private UUID extractUserId(
            String authHeader
    ) {

        String token =
                authHeader.substring(7);

        return jwtUtil.extractUserId(
                token
        );
    }

    private void requireCustomer(
            String authHeader
    ) {

        String token =
                authHeader.substring(7);

        String role =
                jwtUtil.extractRole(token);

        String normalizedRole =
                normalizeRole(role);

        if (!"CUSTOMER".equals(normalizedRole)) {
            throw new ForbiddenOperationException(
                    "Only customers can access the cart"
            );
        }
    }

    private String normalizeRole(
            String role
    ) {

        if (role == null || role.isBlank()) {
            return "";
        }

        String normalized =
                role.trim().toUpperCase(Locale.ROOT);

        if (normalized.startsWith("ROLE_")) {
            normalized =
                    normalized.substring(5);
        }

        return normalized;
    }
}
