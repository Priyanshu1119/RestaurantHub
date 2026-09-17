package com.restaurant.hub.dto.payment;

import com.restaurant.hub.entity.Payment;
import com.restaurant.hub.enums.PaymentStatus;

import java.math.BigDecimal;

public record PaymentResponse(
        Long id,
        Long orderId,
        PaymentStatus status,
        BigDecimal amount,
        String razorpayOrderId,
        String razorpayPaymentId
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(), payment.getOrder().getId(), payment.getStatus(),
                payment.getAmount(), payment.getRazorpayOrderId(), payment.getRazorpayPaymentId()
        );
    }
}
