package com.foodordering.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class PopularMenuItemDto {

    private UUID menuItemId;
    private String itemName;
    private long quantitySold;
    private BigDecimal revenue;

    public PopularMenuItemDto() {
    }

    public PopularMenuItemDto(
            UUID menuItemId,
            String itemName,
            long quantitySold,
            BigDecimal revenue
    ) {
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.quantitySold = quantitySold;
        this.revenue = revenue;
    }

    public UUID getMenuItemId() {
        return menuItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public long getQuantitySold() {
        return quantitySold;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }
}
