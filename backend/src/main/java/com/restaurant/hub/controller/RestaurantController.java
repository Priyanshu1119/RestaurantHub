package com.restaurant.hub.controller;

import com.restaurant.hub.dto.menu.RestaurantResponse;
import com.restaurant.hub.dto.menu.RestaurantUpdateRequest;
import com.restaurant.hub.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "Restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/api/restaurants/{id}")
    @Operation(summary = "Get restaurant profile (public)")
    public ResponseEntity<RestaurantResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getById(id));
    }

    @PutMapping("/api/admin/restaurants/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update restaurant profile")
    public ResponseEntity<RestaurantResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody RestaurantUpdateRequest request) {
        return ResponseEntity.ok(restaurantService.update(id, request));
    }

    @PostMapping(value = "/api/admin/restaurants/{id}/logo", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Upload restaurant logo")
    public ResponseEntity<RestaurantResponse> uploadLogo(@PathVariable Long id,
                                                           @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(restaurantService.updateLogo(id, file));
    }

    @PostMapping(value = "/api/admin/restaurants/{id}/cover", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Upload restaurant cover image")
    public ResponseEntity<RestaurantResponse> uploadCover(@PathVariable Long id,
                                                            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(restaurantService.updateCoverImage(id, file));
    }
}
