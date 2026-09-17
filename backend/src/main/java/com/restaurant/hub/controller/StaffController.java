package com.restaurant.hub.controller;

import com.restaurant.hub.dto.admin.StaffRequest;
import com.restaurant.hub.dto.admin.StaffResponse;
import com.restaurant.hub.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/restaurants/{restaurantId}/staff")
@PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'SUPER_ADMIN')")
@Tag(name = "Admin Staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    @Operation(summary = "List staff accounts for a restaurant")
    public ResponseEntity<List<StaffResponse>> list(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(staffService.listStaff(restaurantId));
    }

    @PostMapping
    @Operation(summary = "Create a staff account for a restaurant")
    public ResponseEntity<StaffResponse> create(@PathVariable Long restaurantId, @Valid @RequestBody StaffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(staffService.createStaff(restaurantId, request));
    }

    @DeleteMapping("/{staffUserId}")
    @Operation(summary = "Remove a staff account from a restaurant")
    public ResponseEntity<Void> remove(@PathVariable Long restaurantId, @PathVariable Long staffUserId) {
        staffService.removeStaff(restaurantId, staffUserId);
        return ResponseEntity.noContent().build();
    }
}
