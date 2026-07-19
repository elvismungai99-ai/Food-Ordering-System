package com.foodordering.order;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "restaurant_name", nullable = false)
    private String restaurantName;

    @Column(
            name = "delivery_address",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(
            name = "total_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal totalAmount;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Order() {
    }


    // =========================================================
    // JPA LIFECYCLE METHODS
    // =========================================================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = OrderStatus.PENDING;
        }

        if (paymentStatus == null) {
            paymentStatus =
                    PaymentStatus.PENDING;
        }

        if (totalAmount == null) {
            totalAmount =
                    BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                LocalDateTime.now();
    }


    // =========================================================
    // ORDER ITEM HELPER METHODS
    // =========================================================

    public void addItem(
            OrderItem item
    ) {

        if (item == null) {
            return;
        }

        items.add(item);

        item.setOrder(this);
    }


    public void removeItem(
            OrderItem item
    ) {

        if (item == null) {
            return;
        }

        items.remove(item);

        item.setOrder(null);
    }


    // =========================================================
    // GETTERS AND SETTERS
    // =========================================================

    public UUID getId() {
        return id;
    }

    public void setId(
            UUID id
    ) {
        this.id = id;
    }


    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(
            UUID customerId
    ) {
        this.customerId =
                customerId;
    }


    public UUID getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(
            UUID restaurantId
    ) {
        this.restaurantId =
                restaurantId;
    }


    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(
            String restaurantName
    ) {
        this.restaurantName =
                restaurantName;
    }


    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(
            String deliveryAddress
    ) {
        this.deliveryAddress =
                deliveryAddress;
    }


    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(
            OrderStatus status
    ) {
        this.status =
                status;
    }


    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(
            PaymentStatus paymentStatus
    ) {
        this.paymentStatus =
                paymentStatus;
    }


    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(
            String paymentReference
    ) {
        this.paymentReference =
                paymentReference;
    }


    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(
            BigDecimal totalAmount
    ) {
        this.totalAmount =
                totalAmount;
    }


    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(
            List<OrderItem> items
    ) {

        this.items.clear();

        if (items != null) {

            for (OrderItem item : items) {
                addItem(item);
            }
        }
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt =
                createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt
    ) {
        this.updatedAt =
                updatedAt;
    }
}