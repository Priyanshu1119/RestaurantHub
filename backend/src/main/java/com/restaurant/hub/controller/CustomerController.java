package com.restaurant.hub.controller;

import com.restaurant.hub.dto.admin.CustomerSummaryResponse;
import com.restaurant.hub.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/customers")
@PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'SUPER_ADMIN')")
@Tag(name = "Admin Customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @Operation(summary = "List customers who have ordered from a restaurant, with order counts and spend")
    public ResponseEntity<List<CustomerSummaryResponse>> list(@RequestParam Long restaurantId) {
        return ResponseEntity.ok(customerService.listByRestaurant(restaurantId));
    }
}
