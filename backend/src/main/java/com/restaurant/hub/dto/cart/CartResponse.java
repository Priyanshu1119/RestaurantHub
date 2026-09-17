package com.restaurant.hub.dto.cart;

import com.restaurant.hub.entity.Cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long id,
        Long restaurantId,
        List<CartItemResponse> items,
        BigDecimal subtotal
) {
    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream().map(CartItemResponse::from).toList();
        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                cart.getRestaurant() != null ? cart.getRestaurant().getId() : null,
                items,
                subtotal
        );
    }
}
