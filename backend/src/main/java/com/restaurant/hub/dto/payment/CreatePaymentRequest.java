package com.restaurant.hub.dto.payment;

import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(

        @NotNull(message = "orderId is required")
        Long orderId
) {}
