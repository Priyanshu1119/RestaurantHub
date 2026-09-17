package com.restaurant.hub.repository;

import com.restaurant.hub.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
    long countByCouponId(Long couponId);
    long countByCouponIdAndUserId(Long couponId, Long userId);
}
