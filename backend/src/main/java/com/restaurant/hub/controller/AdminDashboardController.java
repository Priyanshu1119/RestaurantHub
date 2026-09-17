package com.restaurant.hub.controller;

import com.restaurant.hub.dto.admin.DashboardResponse;
import com.restaurant.hub.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'SUPER_ADMIN')")
@Tag(name = "Admin Dashboard")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(summary = "Today's orders/revenue, pending/completed counts, popular items, coupon and ticket stats")
    public ResponseEntity<DashboardResponse> get(@RequestParam Long restaurantId) {
        return ResponseEntity.ok(dashboardService.getDashboard(restaurantId));
    }
}
