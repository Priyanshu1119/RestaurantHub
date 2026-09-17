package com.restaurant.hub.controller;

import com.restaurant.hub.dto.coupon.*;
import com.restaurant.hub.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/api/coupons")
    @Operation(summary = "List active coupons for a restaurant")
    public ResponseEntity<List<CouponResponse>> list(@RequestParam Long restaurantId) {
        return ResponseEntity.ok(couponService.listByRestaurant(restaurantId));
    }

    @PostMapping("/api/coupons/validate")
    @Operation(summary = "Check a coupon code against the current subtotal before checkout")
    public ResponseEntity<ValidateCouponResponse> validate(@Valid @RequestBody ValidateCouponRequest request) {
        return ResponseEntity.ok(couponService.validate(request.code(), request.restaurantId(), request.subtotal()));
    }

    @PostMapping("/api/admin/coupons")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Create a coupon")
    public ResponseEntity<CouponResponse> create(@Valid @RequestBody CouponRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.create(request));
    }

    @PutMapping("/api/admin/coupons/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update a coupon")
    public ResponseEntity<CouponResponse> update(@PathVariable Long id, @Valid @RequestBody CouponRequest request) {
        return ResponseEntity.ok(couponService.update(id, request));
    }

    @DeleteMapping("/api/admin/coupons/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Delete a coupon")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        couponService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
