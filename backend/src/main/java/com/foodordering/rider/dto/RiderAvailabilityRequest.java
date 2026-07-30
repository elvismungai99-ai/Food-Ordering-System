package com.foodordering.rider.dto;

import com.foodordering.rider.RiderOperationalStatus;

import jakarta.validation.constraints.NotNull;

public class RiderAvailabilityRequest {

    @NotNull(message = "Operational status is required")
    private RiderOperationalStatus operationalStatus;

    private boolean online;

    public RiderOperationalStatus getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(RiderOperationalStatus operationalStatus) {
        this.operationalStatus = operationalStatus;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
