package com.foodordering.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private Order order;

    /*
     * Reference to the original menu item.
     *
     * This can be useful for reporting, but the order must
     * never depend on the current MenuItem price or name.
     */
    @Column(name = "menu_item_id")
    private UUID menuItemId;

    /*
     * Permanent snapshot of the item name.
     */
    @Column(
            name = "item_name",
            nullable = false
    )
    private String itemName;

    /*
     * Permanent snapshot of the description.
     */
    @Column(
            name = "item_description",
            columnDefinition = "TEXT"
    )
    private String itemDescription;

    /*
     * Permanent snapshot of the image.
     */
    @Column(
            name = "image_url",
            columnDefinition = "TEXT"
    )
    private String imageUrl;

    @Column(
            name = "quantity",
            nullable = false
    )
    private Integer quantity;

    /*
     * Final price accepted and charged at checkout.
     */
    @Column(
            name = "unit_price",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal unitPrice;

    /*
     * unitPrice × quantity at purchase time.
     */
    @Column(
            name = "subtotal",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal subtotal;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    public OrderItem() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public UUID getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(UUID menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(
            String itemDescription
    ) {
        this.itemDescription = itemDescription;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(
            BigDecimal unitPrice
    ) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(
            BigDecimal subtotal
    ) {
        this.subtotal = subtotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}