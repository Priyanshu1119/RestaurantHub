package com.restaurant.hub;

import com.restaurant.hub.entity.Coupon;
import com.restaurant.hub.entity.Restaurant;
import com.restaurant.hub.entity.User;
import com.restaurant.hub.enums.DiscountType;
import com.restaurant.hub.exception.InvalidCouponException;
import com.restaurant.hub.repository.CouponRepository;
import com.restaurant.hub.repository.CouponUsageRepository;
import com.restaurant.hub.repository.RestaurantRepository;
import com.restaurant.hub.security.CurrentUserProvider;
import com.restaurant.hub.service.CouponService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock private CouponRepository couponRepository;
    @Mock private CouponUsageRepository couponUsageRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private CouponService couponService;

    private Restaurant restaurant() {
        return Restaurant.builder().id(1L).name("Spice House").build();
    }

    @Test
    void percentageCouponCapsAtMaxDiscount() {
        User user = User.builder().id(1L).build();
        when(currentUserProvider.getCurrentUser()).thenReturn(user);

        Coupon coupon = Coupon.builder().id(1L).restaurant(restaurant()).code("SAVE20")
                .discountType(DiscountType.PERCENTAGE).value(BigDecimal.valueOf(20))
                .minOrderValue(BigDecimal.ZERO).maxDiscount(BigDecimal.valueOf(50))
                .usageLimit(null).perUserLimit(5).active(true).build();

        when(couponRepository.findByRestaurantIdAndCodeIgnoreCase(1L, "SAVE20")).thenReturn(Optional.of(coupon));
        when(couponUsageRepository.countByCouponIdAndUserId(1L, 1L)).thenReturn(0L);

        var result = couponService.validate("SAVE20", 1L, BigDecimal.valueOf(500));
        // 20% of 500 = 100, capped at 50
        assertEquals(0, BigDecimal.valueOf(50).compareTo(result.discountAmount()));
    }

    @Test
    void rejectsCouponBelowMinimumOrderValue() {
        User user = User.builder().id(1L).build();
        when(currentUserProvider.getCurrentUser()).thenReturn(user);

        Coupon coupon = Coupon.builder().id(2L).restaurant(restaurant()).code("SAVE100")
                .discountType(DiscountType.FIXED).value(BigDecimal.valueOf(100))
                .minOrderValue(BigDecimal.valueOf(799)).active(true).build();

        when(couponRepository.findByRestaurantIdAndCodeIgnoreCase(1L, "SAVE100")).thenReturn(Optional.of(coupon));

        assertThrows(InvalidCouponException.class,
                () -> couponService.validate("SAVE100", 1L, BigDecimal.valueOf(500)));
    }

    @Test
    void rejectsExpiredCoupon() {
        User user = User.builder().id(1L).build();
        when(currentUserProvider.getCurrentUser()).thenReturn(user);

        Coupon coupon = Coupon.builder().id(3L).restaurant(restaurant()).code("OLD10")
                .discountType(DiscountType.PERCENTAGE).value(BigDecimal.TEN)
                .minOrderValue(BigDecimal.ZERO)
                .expiryDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .active(true).build();

        when(couponRepository.findByRestaurantIdAndCodeIgnoreCase(1L, "OLD10")).thenReturn(Optional.of(coupon));

        assertThrows(InvalidCouponException.class,
                () -> couponService.validate("OLD10", 1L, BigDecimal.valueOf(500)));
    }

    @Test
    void rejectsWhenPerUserLimitReached() {
        User user = User.builder().id(1L).build();
        when(currentUserProvider.getCurrentUser()).thenReturn(user);

        Coupon coupon = Coupon.builder().id(4L).restaurant(restaurant()).code("ONCE")
                .discountType(DiscountType.FIXED).value(BigDecimal.TEN)
                .minOrderValue(BigDecimal.ZERO).perUserLimit(1).active(true).build();

        when(couponRepository.findByRestaurantIdAndCodeIgnoreCase(1L, "ONCE")).thenReturn(Optional.of(coupon));
        when(couponUsageRepository.countByCouponIdAndUserId(4L, 1L)).thenReturn(1L);

        assertThrows(InvalidCouponException.class,
                () -> couponService.validate("ONCE", 1L, BigDecimal.valueOf(500)));
    }
}
