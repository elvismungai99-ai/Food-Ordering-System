package com.foodordering.analytics.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RestaurantAnalyticsDto {

    private UUID restaurantId;
    private String restaurantName;
    private BigDecimal totalSales = BigDecimal.ZERO;
    private BigDecimal todaySales = BigDecimal.ZERO;
    private BigDecimal averageOrderValue = BigDecimal.ZERO;
    private long totalOrders;
    private long completedOrders;
    private long activeOrders;
    private long cancelledOrders;
    private double cancellationRate;
    private double averagePrepTimeMinutes = 18.0;
    private double averageDeliveryTimeMinutes = 24.0;
    private double fulfillmentRate = 96.0;
    private List<DailySalesDto> dailySales = new ArrayList<>();
    private List<PopularMenuItemDto> popularMenuItems = new ArrayList<>();

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(UUID restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public BigDecimal getTodaySales() {
        return todaySales;
    }

    public void setTodaySales(BigDecimal todaySales) {
        this.todaySales = todaySales;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(long completedOrders) {
        this.completedOrders = completedOrders;
    }

    public long getActiveOrders() {
        return activeOrders;
    }

    public void setActiveOrders(long activeOrders) {
        this.activeOrders = activeOrders;
    }

    public long getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(long cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public double getCancellationRate() {
        return cancellationRate;
    }

    public void setCancellationRate(double cancellationRate) {
        this.cancellationRate = cancellationRate;
    }

    public double getAveragePrepTimeMinutes() {
        return averagePrepTimeMinutes;
    }

    public void setAveragePrepTimeMinutes(double averagePrepTimeMinutes) {
        this.averagePrepTimeMinutes = averagePrepTimeMinutes;
    }

    public double getAverageDeliveryTimeMinutes() {
        return averageDeliveryTimeMinutes;
    }

    public void setAverageDeliveryTimeMinutes(double averageDeliveryTimeMinutes) {
        this.averageDeliveryTimeMinutes = averageDeliveryTimeMinutes;
    }

    public double getFulfillmentRate() {
        return fulfillmentRate;
    }

    public void setFulfillmentRate(double fulfillmentRate) {
        this.fulfillmentRate = fulfillmentRate;
    }

    public List<DailySalesDto> getDailySales() {
        return dailySales;
    }

    public void setDailySales(List<DailySalesDto> dailySales) {
        this.dailySales = dailySales;
    }

    public List<PopularMenuItemDto> getPopularMenuItems() {
        return popularMenuItems;
    }

    public void setPopularMenuItems(List<PopularMenuItemDto> popularMenuItems) {
        this.popularMenuItems = popularMenuItems;
    }
}
