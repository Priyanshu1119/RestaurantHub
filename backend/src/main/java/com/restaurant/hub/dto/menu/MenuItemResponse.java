package com.restaurant.hub.dto.menu;

import com.restaurant.hub.entity.MenuItem;
import com.restaurant.hub.enums.SpiceLevel;

import java.math.BigDecimal;
import java.util.List;

public record MenuItemResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        BigDecimal discountPrice,
        String imageUrl,
        Long categoryId,
        String categoryName,
        Long restaurantId,
        boolean vegetarian,
        SpiceLevel spiceLevel,
        List<String> ingredients,
        List<String> allergens,
        boolean available,
        Integer preparationTimeMinutes,
        boolean featured,
        boolean popular
) {
    public static MenuItemResponse from(MenuItem item) {
        return new MenuItemResponse(
                item.getId(), item.getName(), item.getDescription(),
                item.getPrice(), item.getDiscountPrice(), item.getImageUrl(),
                item.getCategory().getId(), item.getCategory().getName(),
                item.getRestaurant().getId(),
                item.isVegetarian(), item.getSpiceLevel(),
                item.getIngredients(), item.getAllergens(),
                item.isAvailable(), item.getPreparationTimeMinutes(),
                item.isFeatured(), item.isPopular()
        );
    }
}
