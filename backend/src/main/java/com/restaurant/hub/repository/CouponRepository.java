package com.restaurant.hub.repository;

import com.restaurant.hub.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByRestaurantIdAndCodeIgnoreCase(Long restaurantId, String code);
    List<Coupon> findByRestaurantId(Long restaurantId);
}
