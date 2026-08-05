package com.foodordering.order;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.foodordering.payment.PaymentMethod;

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

    @Column(
            name = "delivery_latitude",
            precision = 9,
            scale = 6
    )
    private BigDecimal deliveryLatitude;

    @Column(
            name = "delivery_longitude",
            precision = 10,
            scale = 6
    )
    private BigDecimal deliveryLongitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(
            name = "total_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal totalAmount;

    @Column(
            name = "subtotal_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal subtotalAmount;

    @Column(
            name = "delivery_fee",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal deliveryFee;

    @Column(
            name = "service_fee",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal serviceFee;

    @Column(
            name = "tax_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal taxAmount;

    @Column(
            name = "discount_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal discountAmount;

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

        if (paymentMethod == null) {
            paymentMethod =
                    PaymentMethod.CASH_ON_DELIVERY;
        }

        if (totalAmount == null) {
            totalAmount =
                    BigDecimal.ZERO;
        }

        if (subtotalAmount == null) {
            subtotalAmount =
                    BigDecimal.ZERO;
        }

        if (deliveryFee == null) {
            deliveryFee =
                    BigDecimal.ZERO;
        }

        if (serviceFee == null) {
            serviceFee =
                    BigDecimal.ZERO;
        }

        if (taxAmount == null) {
            taxAmount =
                    BigDecimal.ZERO;
        }

        if (discountAmount == null) {
            discountAmount =
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


    public BigDecimal getDeliveryLatitude() {
        return deliveryLatitude;
    }

    public void setDeliveryLatitude(
            BigDecimal deliveryLatitude
    ) {
        this.deliveryLatitude =
                deliveryLatitude;
    }


    public BigDecimal getDeliveryLongitude() {
        return deliveryLongitude;
    }

    public void setDeliveryLongitude(
            BigDecimal deliveryLongitude
    ) {
        this.deliveryLongitude =
                deliveryLongitude;
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

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(
            PaymentMethod paymentMethod
    ) {
        this.paymentMethod =
                paymentMethod;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(
            String cancellationReason
    ) {
        this.cancellationReason =
                cancellationReason;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(
            LocalDateTime cancelledAt
    ) {
        this.cancelledAt =
                cancelledAt;
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

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(
            BigDecimal subtotalAmount
    ) {
        this.subtotalAmount =
                subtotalAmount;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(
            BigDecimal deliveryFee
    ) {
        this.deliveryFee =
                deliveryFee;
    }

    public BigDecimal getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(
            BigDecimal serviceFee
    ) {
        this.serviceFee =
                serviceFee;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(
            BigDecimal taxAmount
    ) {
        this.taxAmount =
                taxAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(
            BigDecimal discountAmount
    ) {
        this.discountAmount =
                discountAmount;
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
