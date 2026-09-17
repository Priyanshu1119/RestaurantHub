package com.restaurant.hub.dto.coupon;

import java.math.BigDecimal;

public record ValidateCouponResponse(
        String code,
        BigDecimal discountAmount,
        String message
) {}
