package com.restaurant.hub.repository;

import com.restaurant.hub.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi.itemName AS name, SUM(oi.quantity) AS totalQuantity " +
            "FROM OrderItem oi WHERE oi.order.restaurant.id = :restaurantId " +
            "GROUP BY oi.itemName ORDER BY SUM(oi.quantity) DESC")
    List<PopularItemProjection> findPopularItems(@Param("restaurantId") Long restaurantId, Pageable pageable);

    interface PopularItemProjection {
        String getName();
        Long getTotalQuantity();
    }
}
