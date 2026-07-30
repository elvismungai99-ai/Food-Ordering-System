package com.foodordering.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailySalesDto {

    private LocalDate date;
    private BigDecimal sales;
    private long orderCount;

    public DailySalesDto() {
    }

    public DailySalesDto(
            LocalDate date,
            BigDecimal sales,
            long orderCount
    ) {
        this.date = date;
        this.sales = sales;
        this.orderCount = orderCount;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getSales() {
        return sales;
    }

    public long getOrderCount() {
        return orderCount;
    }
}
