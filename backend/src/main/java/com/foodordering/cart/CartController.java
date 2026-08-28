package com.foodordering.cart;

import com.foodordering.User.entity.User;
import com.foodordering.cart.dto.AddCartItemRequest;
import com.foodordering.cart.dto.CartDto;
import com.foodordering.cart.dto.UpdateCartItemRequest;
import com.foodordering.security.SecurityUtils;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final SecurityUtils securityUtils;

    public CartController(
            CartService cartService,
            SecurityUtils securityUtils
    ) {
        this.cartService = cartService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<CartDto> getCart() {
        User customer = securityUtils.requireCustomer();
        return ResponseEntity.ok(
                cartService.getCart(customer.getId())
        );
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(
            @Valid @RequestBody AddCartItemRequest request
    ) {
        User customer = securityUtils.requireCustomer();
        return ResponseEntity.ok(
                cartService.addItem(customer.getId(), request)
        );
    }

    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<CartDto> updateQuantity(
            @PathVariable UUID cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        User customer = securityUtils.requireCustomer();
        return ResponseEntity.ok(
                cartService.updateQuantity(
                        customer.getId(),
                        cartItemId,
                        request.getQuantity()
                )
        );
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartDto> removeItem(
            @PathVariable UUID cartItemId
    ) {
        User customer = securityUtils.requireCustomer();
        return ResponseEntity.ok(
                cartService.removeItem(customer.getId(), cartItemId)
        );
    }

    @PostMapping("/accept-price-changes")
    public ResponseEntity<CartDto> acceptPriceChanges() {
        User customer = securityUtils.requireCustomer();
        return ResponseEntity.ok(
                cartService.acceptPriceChanges(customer.getId())
        );
    }
}
