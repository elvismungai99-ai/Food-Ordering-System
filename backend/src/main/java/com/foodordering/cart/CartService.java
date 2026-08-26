package com.foodordering.cart;

import com.foodordering.cart.dto.CartDto;
import com.foodordering.common.exception.BusinessRuleException;
import com.foodordering.common.exception.ForbiddenOperationException;
import com.foodordering.common.exception.ResourceNotFoundException;

import com.foodordering.menu.MenuItem;
import com.foodordering.menu.MenuItemRepository;
import com.foodordering.restaurant.RestaurantService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantService restaurantService;
    private final com.foodordering.payment.PricingService pricingService;

    public CartService(
            CartRepository cartRepository,
            MenuItemRepository menuItemRepository,
            RestaurantService restaurantService,
            com.foodordering.payment.PricingService pricingService
    ) {
        this.cartRepository =
                cartRepository;

        this.menuItemRepository =
                menuItemRepository;

        this.restaurantService =
                restaurantService;

        this.pricingService =
                pricingService;
    }

    @Transactional
    public CartDto getCart(
            UUID customerId
    ) {

        Cart cart =
                getOrCreateCart(
                        customerId
                );

        return toCartDto(cart);
    }

    @Transactional
    public CartDto addItem(
            UUID customerId,
            UUID menuItemId,
            Integer quantity
    ) {
        com.foodordering.cart.dto.AddCartItemRequest request =
                new com.foodordering.cart.dto.AddCartItemRequest();
        request.setMenuItemId(menuItemId);
        request.setQuantity(quantity);
        return addItem(customerId, request);
    }

    @Transactional
    public CartDto addItem(
            UUID customerId,
            com.foodordering.cart.dto.AddCartItemRequest request
    ) {
        MenuItem menuItem =
                menuItemRepository
                        .findById(request.getMenuItemId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Menu item not found"
                                )
                        );

        if (!menuItem.isAvailable()) {
            throw new BusinessRuleException(
                    menuItem.getName()
                    + " is currently unavailable"
            );
        }

        validateRestaurantAvailableForOrders(
                menuItem
        );

        Cart cart =
                getOrCreateCart(
                        customerId
                );

        /*
         * Prevent mixing restaurants in one cart.
         */
        for (CartItem existing : cart.getItems()) {
            MenuItem existingMenuItem =
                    menuItemRepository
                            .findById(
                                    existing.getMenuItemId()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "A menu item in your cart no longer exists"
                                    )
                            );

            if (
                    !existingMenuItem
                            .getRestaurantId()
                            .equals(
                                    menuItem
                                            .getRestaurantId()
                            )
            ) {
                throw new BusinessRuleException(
                        "Your cart can contain items from only one restaurant at a time"
                );
            }
        }

        String selectedSize = request.getSelectedSize();
        String selectedAddOns = (request.getSelectedAddOns() != null && !request.getSelectedAddOns().isEmpty())
                ? String.join(";;", request.getSelectedAddOns())
                : null;
        String specialInstructions = request.getSpecialInstructions() != null && !request.getSpecialInstructions().isBlank()
                ? request.getSpecialInstructions().trim()
                : null;
        String removalRequests = (request.getRemovalRequests() != null && !request.getRemovalRequests().isEmpty())
                ? String.join(";;", request.getRemovalRequests())
                : null;

        BigDecimal basePrice = menuItem.getPrice() != null ? menuItem.getPrice() : BigDecimal.ZERO;
        BigDecimal extraPrice = request.getExtraPrice() != null ? request.getExtraPrice() : BigDecimal.ZERO;
        BigDecimal finalUnitPrice = basePrice.add(extraPrice);

        /*
         * Same item with identical customizations → increase quantity
         */
        CartItem existingItem =
                cart.getItems()
                        .stream()
                        .filter(item ->
                                item.getMenuItemId().equals(request.getMenuItemId())
                                && java.util.Objects.equals(item.getSelectedSize(), selectedSize)
                                && java.util.Objects.equals(item.getSelectedAddOns(), selectedAddOns)
                                && java.util.Objects.equals(item.getSpecialInstructions(), specialInstructions)
                                && java.util.Objects.equals(item.getRemovalRequests(), removalRequests)
                        )
                        .findFirst()
                        .orElse(null);

        if (existingItem != null) {
            int newQuantity =
                    existingItem.getQuantity()
                    + request.getQuantity();

            if (newQuantity > 99) {
                throw new BusinessRuleException(
                        "Quantity cannot exceed 99"
                );
            }

            existingItem.setQuantity(newQuantity);
        } else {
            CartItem item = new CartItem();
            item.setMenuItemId(menuItem.getId());
            item.setQuantity(request.getQuantity());
            item.setUnitPrice(finalUnitPrice);
            item.setSelectedSize(selectedSize);
            item.setSelectedAddOns(selectedAddOns);
            item.setSpecialInstructions(specialInstructions);
            item.setRemovalRequests(removalRequests);

            cart.addItem(item);
        }

        Cart saved =
                cartRepository.save(cart);

        return toCartDto(saved);
    }

    @Transactional
    public CartDto updateQuantity(
            UUID customerId,
            UUID cartItemId,
            Integer quantity
    ) {

        Cart cart =
                getOrCreateCart(
                        customerId
                );

        CartItem cartItem =
                findOwnedCartItem(
                        cart,
                        cartItemId
                );

        cartItem.setQuantity(
                quantity
        );

        return toCartDto(
                cartRepository.save(
                        cart
                )
        );
    }

    @Transactional
    public CartDto removeItem(
            UUID customerId,
            UUID cartItemId
    ) {

        Cart cart =
                getOrCreateCart(
                        customerId
                );

        CartItem item =
                findOwnedCartItem(
                        cart,
                        cartItemId
                );

        cart.getItems()
                .remove(item);

        return toCartDto(
                cartRepository.save(
                        cart
                )
        );
    }

    @Transactional
    public CartDto acceptPriceChanges(
            UUID customerId
    ) {

        Cart cart =
                getOrCreateCart(
                        customerId
                );

        if (cart.getItems().isEmpty()) {
            throw new BusinessRuleException(
                    "Your cart is empty"
            );
        }

        for (
                CartItem cartItem :
                cart.getItems()
        ) {

            MenuItem menuItem =
                    menuItemRepository
                            .findById(
                                    cartItem
                                            .getMenuItemId()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "A menu item in your cart no longer exists"
                                    )
                            );

            if (!menuItem.isAvailable()) {
                throw new BusinessRuleException(
                        menuItem.getName()
                        + " is no longer available"
                );
            }

            validateRestaurantAvailableForOrders(
                    menuItem
            );

            cartItem.setUnitPrice(
                    menuItem.getPrice()
            );
        }

        return toCartDto(
                cartRepository.save(
                        cart
                )
        );
    }

    private CartDto toCartDto(
            Cart cart
    ) {

        List<UUID> menuItemIds =
                cart.getItems()
                        .stream()
                        .map(CartItem::getMenuItemId)
                        .toList();

        Map<UUID, MenuItem> menuItemsById =
                menuItemRepository
                        .findAllById(menuItemIds)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        MenuItem::getId,
                                        Function.identity()
                                )
                        );

        CartDto cartDto =
                new CartDto();

        cartDto.setId(
                cart.getId()
        );

        cartDto.setCustomerId(
                cart.getCustomerId()
        );

        BigDecimal previousTotal =
                BigDecimal.ZERO;

        BigDecimal currentTotal =
                BigDecimal.ZERO;

        int itemCount =
                0;

        boolean hasPriceChanges =
                false;

        boolean hasUnavailableItems =
                false;

        for (
                CartItem cartItem :
                cart.getItems()
        ) {

            MenuItem menuItem =
                    menuItemsById.get(
                            cartItem.getMenuItemId()
                    );

            com.foodordering.cart.dto.CartItemDto itemDto =
                    new com.foodordering.cart.dto.CartItemDto(
                            cartItem,
                            menuItem
                    );

            cartDto
                    .getItems()
                    .add(itemDto);

            previousTotal =
                    previousTotal.add(
                            itemDto.getSubtotal()
                    );

            currentTotal =
                    currentTotal.add(
                            itemDto.getCurrentSubtotal()
                    );

            if (cartItem.getQuantity() != null) {
                itemCount +=
                        cartItem.getQuantity();
            }

            hasPriceChanges =
                    hasPriceChanges
                    || itemDto.isPriceChanged();

            hasUnavailableItems =
                    hasUnavailableItems
                    || !itemDto.isAvailable();
        }

        cartDto.setTotalItems(
                itemCount
        );

        cartDto.setPreviousTotalAmount(
                previousTotal
        );

        cartDto.setTotalAmount(
                currentTotal
        );

        cartDto.setSubtotalAmount(
                currentTotal
        );

        com.foodordering.payment.PricingBreakdown pricing =
                pricingService.calculate(currentTotal);

        cartDto.setDeliveryFee(pricing.deliveryFee());
        cartDto.setServiceFee(pricing.serviceFee());
        cartDto.setTaxAmount(pricing.taxAmount());
        cartDto.setDiscountAmount(pricing.discountAmount());
        cartDto.setFinalTotalAmount(pricing.totalAmount());

        cartDto.setHasPriceChanges(
                hasPriceChanges
        );

        cartDto.setHasUnavailableItems(
                hasUnavailableItems
        );

        return cartDto;
    }

    private Cart getOrCreateCart(
            UUID customerId
    ) {

        return cartRepository
                .findWithItemsByCustomerId(
                        customerId
                )
                .orElseGet(() -> {

                    Cart cart =
                            new Cart();

                    cart.setCustomerId(
                            customerId
                    );

                    return cartRepository
                            .save(cart);
                });
    }

    private CartItem findOwnedCartItem(
            Cart cart,
            UUID cartItemId
    ) {

        CartItem item =
                cart.getItems()
                        .stream()
                        .filter(cartItem ->
                                cartItem
                                        .getId()
                                        .equals(
                                                cartItemId
                                        )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Cart item not found"
                                )
                        );

        if (
                !cart
                        .getItems()
                        .contains(item)
        ) {
            throw new ForbiddenOperationException(
                    "You are not allowed to modify this cart item"
            );
        }

        return item;
    }

    private void validateRestaurantAvailableForOrders(
            MenuItem menuItem
    ) {

        if (
                menuItem == null
                || !restaurantService.isApprovedAndOpen(
                        menuItem.getRestaurant()
                )
        ) {
            throw new BusinessRuleException(
                    "This restaurant is not open for orders right now"
            );
        }
    }
}
