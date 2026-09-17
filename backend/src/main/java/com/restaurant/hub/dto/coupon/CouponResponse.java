package com.restaurant.hub.dto.coupon;

import com.restaurant.hub.entity.Coupon;
import com.restaurant.hub.enums.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;

public record CouponResponse(
        Long id,
        String code,
        String description,
        DiscountType discountType,
        BigDecimal value,
        BigDecimal minOrderValue,
        BigDecimal maxDiscount,
        Instant startDate,
        Instant expiryDate,
        Integer usageLimit,
        Integer perUserLimit,
        boolean active
) {
    public static CouponResponse from(Coupon c) {
        return new CouponResponse(c.getId(), c.getCode(), c.getDescription(), c.getDiscountType(),
                c.getValue(), c.getMinOrderValue(), c.getMaxDiscount(), c.getStartDate(), c.getExpiryDate(),
                c.getUsageLimit(), c.getPerUserLimit(), c.isActive());
    }
}
