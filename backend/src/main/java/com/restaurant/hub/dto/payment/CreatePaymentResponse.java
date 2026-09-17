package com.restaurant.hub.dto.payment;

public record CreatePaymentResponse(
        Long orderId,
        String razorpayOrderId,
        long amountInPaise,
        String currency,
        String razorpayKeyId
) {}
