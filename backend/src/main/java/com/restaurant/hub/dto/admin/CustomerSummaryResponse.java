package com.restaurant.hub.dto.admin;

import java.math.BigDecimal;
import java.time.Instant;

public record CustomerSummaryResponse(
        Long userId,
        String name,
        String email,
        long totalOrders,
        BigDecimal totalSpent,
        Instant lastOrderAt
) {}
