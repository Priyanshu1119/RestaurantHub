package com.restaurant.hub.dto.order;

import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(

        @NotNull(message = "Delivery address is required")
        Long addressId,

        // Optional. Re-validated server-side against the live cart total.
        String couponCode
) {}
