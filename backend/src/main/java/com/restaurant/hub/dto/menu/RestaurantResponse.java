package com.restaurant.hub.dto.menu;

import com.restaurant.hub.entity.Restaurant;

import java.math.BigDecimal;

public record RestaurantResponse(
        Long id,
        String name,
        String description,
        String address,
        String phone,
        String email,
        String logoUrl,
        String coverImageUrl,
        String openingHours,
        BigDecimal deliveryFee,
        BigDecimal taxPercentage,
        BigDecimal minimumOrder
) {
    public static RestaurantResponse from(Restaurant r) {
        return new RestaurantResponse(
                r.getId(), r.getName(), r.getDescription(), r.getAddress(), r.getPhone(), r.getEmail(),
                r.getLogoUrl(), r.getCoverImageUrl(), r.getOpeningHours(),
                r.getDeliveryFee(), r.getTaxPercentage(), r.getMinimumOrder()
        );
    }
}
