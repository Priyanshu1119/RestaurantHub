package com.restaurant.hub.specification;

import com.restaurant.hub.entity.MenuItem;
import org.springframework.data.jpa.domain.Specification;

public class MenuItemSpecification {

    private MenuItemSpecification() {}

    public static Specification<MenuItem> hasRestaurant(Long restaurantId) {
        return (root, query, cb) ->
                restaurantId == null ? null : cb.equal(root.get("restaurant").get("id"), restaurantId);
    }

    public static Specification<MenuItem> hasCategory(Long categoryId) {
        return (root, query, cb) ->
                categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<MenuItem> isVegetarian(Boolean veg) {
        return (root, query, cb) ->
                veg == null ? null : cb.equal(root.get("vegetarian"), veg);
    }

    public static Specification<MenuItem> isAvailable(Boolean available) {
        return (root, query, cb) ->
                available == null ? null : cb.equal(root.get("available"), available);
    }

    public static Specification<MenuItem> nameOrDescriptionContains(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }
}
