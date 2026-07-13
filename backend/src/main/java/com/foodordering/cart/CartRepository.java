package com.foodordering.cart;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository
        extends JpaRepository<Cart, UUID> {

    boolean existsByCustomerId(UUID customerId);

    Optional<Cart> findByCustomerId(UUID customerId);

    @EntityGraph(attributePaths = "items")
    Optional<Cart> findWithItemsByCustomerId(UUID customerId);
}