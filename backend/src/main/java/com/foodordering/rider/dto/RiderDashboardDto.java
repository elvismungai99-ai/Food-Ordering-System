package com.foodordering.rider.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RiderDashboardDto {

    private RiderDto rider;
    private BigDecimal totalEarnings = BigDecimal.ZERO;
    private BigDecimal pendingPayout = BigDecimal.ZERO;
    private long completedDeliveries;
    private long rejectedRequests;
    private List<DeliveryRequestDto> deliveryRequests = new ArrayList<>();

    public RiderDto getRider() {
        return rider;
    }

    public void setRider(RiderDto rider) {
        this.rider = rider;
    }

    public BigDecimal getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(BigDecimal totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public BigDecimal getPendingPayout() {
        return pendingPayout;
    }

    public void setPendingPayout(BigDecimal pendingPayout) {
        this.pendingPayout = pendingPayout;
    }

    public long getCompletedDeliveries() {
        return completedDeliveries;
    }

    public void setCompletedDeliveries(long completedDeliveries) {
        this.completedDeliveries = completedDeliveries;
    }

    public long getRejectedRequests() {
        return rejectedRequests;
    }

    public void setRejectedRequests(long rejectedRequests) {
        this.rejectedRequests = rejectedRequests;
    }

    public List<DeliveryRequestDto> getDeliveryRequests() {
        return deliveryRequests;
    }

    public void setDeliveryRequests(List<DeliveryRequestDto> deliveryRequests) {
        this.deliveryRequests = deliveryRequests;
    }
}
