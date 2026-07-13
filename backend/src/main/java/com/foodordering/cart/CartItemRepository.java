package com.foodordering.cart;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository
        extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCartIdAndMenuItemId(
            UUID cartId,
            UUID menuItemId
    );

    Optional<CartItem> findByIdAndCartId(
            UUID cartItemId,
            UUID cartId
    );

    void deleteAllByCartId(UUID cartId);
}