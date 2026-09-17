package com.restaurant.hub.dto.coupon;

import com.restaurant.hub.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record CouponRequest(

        @NotBlank(message = "Coupon code is required")
        String code,

        String description,

        @NotNull(message = "Discount type is required")
        DiscountType discountType,

        @NotNull(message = "Value is required")
        @Positive(message = "Value must be greater than zero")
        BigDecimal value,

        BigDecimal minOrderValue,
        BigDecimal maxDiscount,
        Instant startDate,
        Instant expiryDate,
        Integer usageLimit,
        Integer perUserLimit,
        boolean active,

        // Only read for SUPER_ADMIN requests.
        Long restaurantId
) {}
