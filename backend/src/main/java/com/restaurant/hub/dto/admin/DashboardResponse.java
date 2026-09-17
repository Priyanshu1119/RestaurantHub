package com.restaurant.hub.dto.admin;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        long todayOrders,
        BigDecimal todayRevenue,
        long pendingOrders,
        long completedOrders,
        long totalCustomers,
        List<PopularItem> popularItems,
        long couponUsageCount,
        long eventTicketsSold
) {
    public record PopularItem(String name, long totalQuantity) {}
}
