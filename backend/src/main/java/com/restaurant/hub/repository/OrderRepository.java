package com.restaurant.hub.repository;

import com.restaurant.hub.entity.Order;
import com.restaurant.hub.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserId(Long userId, Pageable pageable);
    Page<Order> findByRestaurantId(Long restaurantId, Pageable pageable);
    Page<Order> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status, Pageable pageable);

    long countByRestaurantIdAndCreatedAtAfter(Long restaurantId, Instant since);
    long countByRestaurantIdAndStatusIn(Long restaurantId, Collection<OrderStatus> statuses);
    long countByRestaurantIdAndStatus(Long restaurantId, OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.restaurant.id = :restaurantId AND o.createdAt >= :since AND o.status <> 'CANCELLED'")
    BigDecimal sumRevenueSince(@Param("restaurantId") Long restaurantId, @Param("since") Instant since);

    @Query("SELECT COUNT(DISTINCT o.user.id) FROM Order o WHERE o.restaurant.id = :restaurantId")
    long countDistinctCustomers(@Param("restaurantId") Long restaurantId);

    @Query("SELECT o.user.id AS userId, o.user.name AS name, o.user.email AS email, " +
            "COUNT(o) AS totalOrders, COALESCE(SUM(o.totalAmount), 0) AS totalSpent, MAX(o.createdAt) AS lastOrderAt " +
            "FROM Order o WHERE o.restaurant.id = :restaurantId GROUP BY o.user.id, o.user.name, o.user.email")
    List<CustomerSummaryProjection> findCustomerSummaries(@Param("restaurantId") Long restaurantId);

    interface CustomerSummaryProjection {
        Long getUserId();
        String getName();
        String getEmail();
        Long getTotalOrders();
        BigDecimal getTotalSpent();
        Instant getLastOrderAt();
    }
}
