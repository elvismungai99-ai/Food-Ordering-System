package com.foodordering.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodordering.cart.dto.AddCartItemRequest;
import com.foodordering.cart.dto.CartDto;
import com.foodordering.cart.dto.CartItemDto;
import com.foodordering.cart.dto.UpdateCartItemRequest;
import com.foodordering.menu.MenuItem;
import com.foodordering.menu.MenuItemRepository;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            MenuItemRepository menuItemRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.menuItemRepository = menuItemRepository;
    }

    public CartDto getCart(UUID customerId) {
        Cart cart = getOrCreateCart(customerId);

        return convertToDto(cart);
    }

    public CartDto addItem(
            UUID customerId,
            AddCartItemRequest request
    ) {
        validateAddItemRequest(request);

        Cart cart = getOrCreateCart(customerId);

        MenuItem menuItem = menuItemRepository
                .findById(request.getMenuItemId())
                .orElseThrow(() ->
                        new RuntimeException("Menu item not found")
                );

        if (!menuItem.isAvailable()) {
            throw new RuntimeException(
                    "This menu item is currently unavailable"
            );
        }

        CartItem existingItem = cartItemRepository
                .findByCartIdAndMenuItemId(
                        cart.getId(),
                        menuItem.getId()
                )
                .orElse(null);

        if (existingItem != null) {
            int newQuantity =
                    existingItem.getQuantity()
                            + request.getQuantity();

            existingItem.setQuantity(newQuantity);
            existingItem.setUnitPrice(menuItem.getPrice());

            cartItemRepository.save(existingItem);
        } else {
            CartItem cartItem = new CartItem();

            cartItem.setCart(cart);
            cartItem.setMenuItemId(menuItem.getId());
            cartItem.setQuantity(request.getQuantity());
            cartItem.setUnitPrice(menuItem.getPrice());

            cart.addItem(cartItem);

            cartRepository.save(cart);
        }

        Cart refreshedCart = cartRepository
                .findWithItemsByCustomerId(customerId)
                .orElseThrow(() ->
                        new RuntimeException("Cart not found")
                );

        return convertToDto(refreshedCart);
    }

    public CartDto updateQuantity(
            UUID customerId,
            UUID cartItemId,
            UpdateCartItemRequest request
    ) {
        if (
                request == null
                || request.getQuantity() == null
        ) {
            throw new RuntimeException(
                    "Quantity is required"
            );
        }

        Cart cart = getExistingCart(customerId);

        CartItem cartItem = cartItemRepository
                .findByIdAndCartId(
                        cartItemId,
                        cart.getId()
                )
                .orElseThrow(() ->
                        new RuntimeException("Cart item not found")
                );

        if (request.getQuantity() <= 0) {
            cart.removeItem(cartItem);
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(request.getQuantity());
            cartItemRepository.save(cartItem);
        }

        Cart refreshedCart = cartRepository
                .findWithItemsByCustomerId(customerId)
                .orElseThrow(() ->
                        new RuntimeException("Cart not found")
                );

        return convertToDto(refreshedCart);
    }

    public CartDto removeItem(
            UUID customerId,
            UUID cartItemId
    ) {
        Cart cart = getExistingCart(customerId);

        CartItem cartItem = cartItemRepository
                .findByIdAndCartId(
                        cartItemId,
                        cart.getId()
                )
                .orElseThrow(() ->
                        new RuntimeException("Cart item not found")
                );

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        Cart refreshedCart = cartRepository
                .findWithItemsByCustomerId(customerId)
                .orElseThrow(() ->
                        new RuntimeException("Cart not found")
                );

        return convertToDto(refreshedCart);
    }

    private Cart getOrCreateCart(UUID customerId) {
        return cartRepository
                .findWithItemsByCustomerId(customerId)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setCustomerId(customerId);

                    return cartRepository.save(cart);
                });
    }

    private Cart getExistingCart(UUID customerId) {
        return cartRepository
                .findWithItemsByCustomerId(customerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Cart not found for this customer"
                        )
                );
    }

    private void validateAddItemRequest(
            AddCartItemRequest request
    ) {
        if (request == null) {
            throw new RuntimeException(
                    "Add-to-cart request is required"
            );
        }

        if (request.getMenuItemId() == null) {
            throw new RuntimeException(
                    "Menu item ID is required"
            );
        }

        if (
                request.getQuantity() == null
                || request.getQuantity() <= 0
        ) {
            throw new RuntimeException(
                    "Quantity must be greater than zero"
            );
        }
    }

    private CartDto convertToDto(Cart cart) {
        CartDto dto = new CartDto();

        dto.setId(cart.getId());
        dto.setCustomerId(cart.getCustomerId());

        List<CartItemDto> itemDtos =
                new ArrayList<>();

        int totalItems = 0;
        BigDecimal totalAmount =
                BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            MenuItem menuItem = menuItemRepository
                    .findById(cartItem.getMenuItemId())
                    .orElse(null);

            CartItemDto itemDto =
                    new CartItemDto(
                            cartItem,
                            menuItem
                    );

            itemDtos.add(itemDto);

            totalItems += cartItem.getQuantity();

            totalAmount = totalAmount.add(
                    cartItem.calculateSubtotal()
            );
        }

        dto.setItems(itemDtos);
        dto.setTotalItems(totalItems);
        dto.setTotalAmount(totalAmount);

        return dto;
    }
}