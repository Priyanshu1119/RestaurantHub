package com.restaurant.hub.dto.cart;

import com.restaurant.hub.entity.CartItem;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long menuItemId,
        String name,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineTotal
) {
    public static CartItemResponse from(CartItem item) {
        BigDecimal unitPrice = item.getMenuItem().getDiscountPrice() != null
                ? item.getMenuItem().getDiscountPrice()
                : item.getMenuItem().getPrice();
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemResponse(
                item.getId(), item.getMenuItem().getId(), item.getMenuItem().getName(),
                unitPrice, item.getQuantity(), lineTotal
        );
    }
}
