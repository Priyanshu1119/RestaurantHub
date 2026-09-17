package com.restaurant.hub.dto.order;

import com.restaurant.hub.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        String itemName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineTotal
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(item.getId(), item.getItemName(), item.getUnitPrice(),
                item.getQuantity(), item.getLineTotal());
    }
}
