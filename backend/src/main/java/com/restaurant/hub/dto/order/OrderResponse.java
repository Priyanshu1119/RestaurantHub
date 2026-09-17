package com.restaurant.hub.dto.order;

import com.restaurant.hub.entity.Order;
import com.restaurant.hub.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        Long restaurantId,
        String restaurantName,
        OrderStatus status,
        String deliveryAddress,
        String contactPhone,
        List<OrderItemResponse> items,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal deliveryFee,
        BigDecimal totalAmount,
        Instant createdAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getRestaurant().getId(),
                order.getRestaurant().getName(),
                order.getStatus(),
                order.getDeliveryAddress(),
                order.getContactPhone(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getTaxAmount(),
                order.getDeliveryFee(),
                order.getTotalAmount(),
                order.getCreatedAt()
        );
    }
}
