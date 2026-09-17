package com.restaurant.hub.dto.coupon;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ValidateCouponRequest(

        @NotBlank(message = "Coupon code is required")
        String code,

        @NotNull(message = "restaurantId is required")
        Long restaurantId,

        @NotNull(message = "subtotal is required")
        java.math.BigDecimal subtotal
) {}
