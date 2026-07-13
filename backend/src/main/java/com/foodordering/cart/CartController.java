package com.foodordering.cart;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodordering.cart.dto.AddCartItemRequest;
import com.foodordering.cart.dto.CartDto;
import com.foodordering.cart.dto.UpdateCartItemRequest;
import com.foodordering.security.JwtUtil;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final JwtUtil jwtUtil;

    public CartController(
            CartService cartService,
            JwtUtil jwtUtil
    ) {
        this.cartService = cartService;
        this.jwtUtil = jwtUtil;
    }

    // View the logged-in customer's cart.
    @GetMapping
    public ResponseEntity<?> getCart(
            @RequestHeader("Authorization")
            String authHeader
    ) {
        try {
            UUID customerId =
                    extractUserId(authHeader);

            CartDto cart =
                    cartService.getCart(customerId);

            return ResponseEntity.ok(cart);

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }

    // Add a menu item to the logged-in customer's cart.
    @PostMapping("/items")
    public ResponseEntity<?> addItem(
            @RequestHeader("Authorization")
            String authHeader,

            @RequestBody
            AddCartItemRequest request
    ) {
        try {
            UUID customerId =
                    extractUserId(authHeader);

            CartDto cart =
                    cartService.addItem(
                            customerId,
                            request
                    );

            return ResponseEntity.ok(cart);

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }

    // Change the quantity of one cart item.
    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<?> updateQuantity(
            @RequestHeader("Authorization")
            String authHeader,

            @PathVariable
            UUID cartItemId,

            @RequestBody
            UpdateCartItemRequest request
    ) {
        try {
            UUID customerId =
                    extractUserId(authHeader);

            CartDto cart =
                    cartService.updateQuantity(
                            customerId,
                            cartItemId,
                            request
                    );

            return ResponseEntity.ok(cart);

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }

    // Remove one cart item.
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<?> removeItem(
            @RequestHeader("Authorization")
            String authHeader,

            @PathVariable
            UUID cartItemId
    ) {
        try {
            UUID customerId =
                    extractUserId(authHeader);

            CartDto cart =
                    cartService.removeItem(
                            customerId,
                            cartItemId
                    );

            return ResponseEntity.ok(cart);

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());
        }
    }

    private UUID extractUserId(String authHeader) {
        if (
                authHeader == null
                || !authHeader.startsWith("Bearer ")
        ) {
            throw new RuntimeException(
                    "Authorization token is missing or invalid"
            );
        }

        String token = authHeader.substring(7);

        return jwtUtil.extractUserId(token);
    }
}