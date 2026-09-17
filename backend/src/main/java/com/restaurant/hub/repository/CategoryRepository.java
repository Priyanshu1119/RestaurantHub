package com.restaurant.hub.repository;

import com.restaurant.hub.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByRestaurantIdOrderByDisplayOrderAsc(Long restaurantId);
}
