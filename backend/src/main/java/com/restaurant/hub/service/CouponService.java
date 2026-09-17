package com.restaurant.hub.service;

import com.restaurant.hub.dto.coupon.CouponRequest;
import com.restaurant.hub.dto.coupon.CouponResponse;
import com.restaurant.hub.dto.coupon.ValidateCouponResponse;
import com.restaurant.hub.entity.*;
import com.restaurant.hub.enums.DiscountType;
import com.restaurant.hub.enums.RoleName;
import com.restaurant.hub.exception.InvalidCouponException;
import com.restaurant.hub.exception.ResourceNotFoundException;
import com.restaurant.hub.exception.UnauthorizedException;
import com.restaurant.hub.repository.CouponRepository;
import com.restaurant.hub.repository.CouponUsageRepository;
import com.restaurant.hub.repository.RestaurantRepository;
import com.restaurant.hub.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final RestaurantRepository restaurantRepository;
    private final CurrentUserProvider currentUserProvider;

    public CouponService(CouponRepository couponRepository, CouponUsageRepository couponUsageRepository,
                          RestaurantRepository restaurantRepository, CurrentUserProvider currentUserProvider) {
        this.couponRepository = couponRepository;
        this.couponUsageRepository = couponUsageRepository;
        this.restaurantRepository = restaurantRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public List<CouponResponse> listByRestaurant(Long restaurantId) {
        return couponRepository.findByRestaurantId(restaurantId).stream().map(CouponResponse::from).toList();
    }

    @Transactional
    public CouponResponse create(CouponRequest request) {
        Long restaurantId = resolveRestaurantId(request.restaurantId());
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id " + restaurantId));

        Coupon coupon = Coupon.builder()
                .restaurant(restaurant)
                .code(request.code().toUpperCase())
                .description(request.description())
                .discountType(request.discountType())
                .value(request.value())
                .minOrderValue(request.minOrderValue() != null ? request.minOrderValue() : BigDecimal.ZERO)
                .maxDiscount(request.maxDiscount())
                .startDate(request.startDate())
                .expiryDate(request.expiryDate())
                .usageLimit(request.usageLimit())
                .perUserLimit(request.perUserLimit() != null ? request.perUserLimit() : 1)
                .active(request.active())
                .build();

        return CouponResponse.from(couponRepository.save(coupon));
    }

    @Transactional
    public CouponResponse update(Long id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id " + id));
        assertCanManage(coupon.getRestaurant().getId());

        coupon.setCode(request.code().toUpperCase());
        coupon.setDescription(request.description());
        coupon.setDiscountType(request.discountType());
        coupon.setValue(request.value());
        if (request.minOrderValue() != null) coupon.setMinOrderValue(request.minOrderValue());
        coupon.setMaxDiscount(request.maxDiscount());
        coupon.setStartDate(request.startDate());
        coupon.setExpiryDate(request.expiryDate());
        coupon.setUsageLimit(request.usageLimit());
        if (request.perUserLimit() != null) coupon.setPerUserLimit(request.perUserLimit());
        coupon.setActive(request.active());

        return CouponResponse.from(coupon);
    }

    @Transactional
    public void delete(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id " + id));
        assertCanManage(coupon.getRestaurant().getId());
        couponRepository.delete(coupon);
    }

    /** Read-only check used by the "apply coupon" button before checkout. */
    public ValidateCouponResponse validate(String code, Long restaurantId, BigDecimal subtotal) {
        User current = currentUserProvider.getCurrentUser();
        Coupon coupon = getActiveCouponOrThrow(code, restaurantId, subtotal, current);
        BigDecimal discount = computeDiscount(coupon, subtotal);
        return new ValidateCouponResponse(coupon.getCode(), discount, "Coupon applied");
    }

    /**
     * Used by OrderService during checkout. Re-validates everything (a coupon
     * could expire or hit its limit between the "apply" click and checkout)
     * and returns the discount to subtract from the order total. Recording
     * the usage row is the caller's job, once the order is actually saved.
     */
    public BigDecimal computeDiscountForCheckout(String code, Restaurant restaurant, BigDecimal subtotal, User user) {
        Coupon coupon = getActiveCouponOrThrow(code, restaurant.getId(), subtotal, user);
        return computeDiscount(coupon, subtotal);
    }

    @Transactional
    public void recordUsage(String code, Long restaurantId, User user, Order order) {
        Coupon coupon = couponRepository.findByRestaurantIdAndCodeIgnoreCase(restaurantId, code)
                .orElseThrow(() -> new InvalidCouponException("Coupon not found"));
        couponUsageRepository.save(CouponUsage.builder().coupon(coupon).user(user).order(order).build());
    }

    private Coupon getActiveCouponOrThrow(String code, Long restaurantId, BigDecimal subtotal, User user) {
        Coupon coupon = couponRepository.findByRestaurantIdAndCodeIgnoreCase(restaurantId, code)
                .orElseThrow(() -> new InvalidCouponException("Invalid coupon code"));

        if (!coupon.isActive()) {
            throw new InvalidCouponException("This coupon is no longer active");
        }
        Instant now = Instant.now();
        if (coupon.getStartDate() != null && now.isBefore(coupon.getStartDate())) {
            throw new InvalidCouponException("This coupon is not active yet");
        }
        if (coupon.getExpiryDate() != null && now.isAfter(coupon.getExpiryDate())) {
            throw new InvalidCouponException("This coupon has expired");
        }
        if (subtotal.compareTo(coupon.getMinOrderValue()) < 0) {
            throw new InvalidCouponException("Minimum order value for this coupon is " + coupon.getMinOrderValue());
        }
        if (coupon.getUsageLimit() != null && couponUsageRepository.countByCouponId(coupon.getId()) >= coupon.getUsageLimit()) {
            throw new InvalidCouponException("This coupon has reached its usage limit");
        }
        if (coupon.getPerUserLimit() != null
                && couponUsageRepository.countByCouponIdAndUserId(coupon.getId(), user.getId()) >= coupon.getPerUserLimit()) {
            throw new InvalidCouponException("You have already used this coupon the maximum number of times");
        }

        return coupon;
    }

    private BigDecimal computeDiscount(Coupon coupon, BigDecimal subtotal) {
        BigDecimal discount = coupon.getDiscountType() == DiscountType.PERCENTAGE
                ? subtotal.multiply(coupon.getValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : coupon.getValue();

        if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
            discount = coupon.getMaxDiscount();
        }
        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }
        return discount;
    }

    private Long resolveRestaurantId(Long requestedRestaurantId) {
        User current = currentUserProvider.getCurrentUser();
        if (current.getRole() == RoleName.SUPER_ADMIN) {
            if (requestedRestaurantId == null) {
                throw new IllegalArgumentException("restaurantId is required for super admin requests");
            }
            return requestedRestaurantId;
        }
        if (current.getRestaurant() == null) {
            throw new UnauthorizedException("Your account is not linked to a restaurant");
        }
        return current.getRestaurant().getId();
    }

    private void assertCanManage(Long restaurantId) {
        User current = currentUserProvider.getCurrentUser();
        if (current.getRole() == RoleName.SUPER_ADMIN) {
            return;
        }
        if (current.getRestaurant() == null || !current.getRestaurant().getId().equals(restaurantId)) {
            throw new UnauthorizedException("You do not have permission to manage this coupon");
        }
    }
}
