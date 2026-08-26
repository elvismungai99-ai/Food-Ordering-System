package com.foodordering.cart.dto;

import com.foodordering.cart.Cart;
import com.foodordering.cart.CartItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CartDto {

    private UUID id;
    private UUID customerId;

    private List<CartItemDto> items =
            new ArrayList<>();

    private Integer totalItems;

    /*
     * Total using the prices stored in cart_items.
     */
    private BigDecimal previousTotalAmount;

    /*
     * Total using current menu prices (subtotal).
     */
    private BigDecimal totalAmount;
    private BigDecimal subtotalAmount;
    private BigDecimal deliveryFee = BigDecimal.valueOf(150.00);
    private BigDecimal serviceFee = BigDecimal.valueOf(35.00);
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal finalTotalAmount;

    private boolean hasPriceChanges;
    private boolean hasUnavailableItems;

    public CartDto() {
    }

    public CartDto(
            Cart cart
    ) {
        this.id = cart.getId();
        this.customerId = cart.getCustomerId();

        BigDecimal total =
                BigDecimal.ZERO;

        int itemCount = 0;

        for (CartItem item : cart.getItems()) {
            CartItemDto itemDto =
                    new CartItemDto();

            itemDto.setId(
                    item.getId()
            );

            itemDto.setMenuItemId(
                    item.getMenuItemId()
            );

            itemDto.setQuantity(
                    item.getQuantity()
            );

            itemDto.setUnitPrice(
                    item.getUnitPrice()
            );

            itemDto.setCurrentPrice(
                    item.getUnitPrice()
            );

            BigDecimal subtotal =
                    item.calculateSubtotal();

            itemDto.setSubtotal(
                    subtotal
            );

            itemDto.setCurrentSubtotal(
                    subtotal
            );

            itemDto.setAvailable(true);
            itemDto.setPriceChanged(false);

            this.items.add(itemDto);

            total =
                    total.add(subtotal);

            if (item.getQuantity() != null) {
                itemCount += item.getQuantity();
            }
        }

        this.totalItems = itemCount;
        this.previousTotalAmount = total;
        this.totalAmount = total;
        this.hasPriceChanges = false;
        this.hasUnavailableItems = false;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(
            List<CartItemDto> items
    ) {
        this.items = items;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(
            Integer totalItems
    ) {
        this.totalItems = totalItems;
    }

    public BigDecimal getPreviousTotalAmount() {
        return previousTotalAmount;
    }

    public void setPreviousTotalAmount(
            BigDecimal previousTotalAmount
    ) {
        this.previousTotalAmount =
                previousTotalAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(
            BigDecimal totalAmount
    ) {
        this.totalAmount = totalAmount;
    }

    public boolean isHasPriceChanges() {
        return hasPriceChanges;
    }

    public void setHasPriceChanges(
            boolean hasPriceChanges
    ) {
        this.hasPriceChanges =
                hasPriceChanges;
    }

    public boolean isHasUnavailableItems() {
        return hasUnavailableItems;
    }

    public void setHasUnavailableItems(
            boolean hasUnavailableItems
    ) {
        this.hasUnavailableItems =
                hasUnavailableItems;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount != null ? subtotalAmount : totalAmount;
    }

    public void setSubtotalAmount(BigDecimal subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public BigDecimal getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(BigDecimal serviceFee) {
        this.serviceFee = serviceFee;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getFinalTotalAmount() {
        return finalTotalAmount != null ? finalTotalAmount : (totalAmount != null ? totalAmount.add(deliveryFee).add(serviceFee) : BigDecimal.ZERO);
    }

    public void setFinalTotalAmount(BigDecimal finalTotalAmount) {
        this.finalTotalAmount = finalTotalAmount;
    }
}
