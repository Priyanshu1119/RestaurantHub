package com.restaurant.hub.controller;

import com.restaurant.hub.dto.menu.MenuItemRequest;
import com.restaurant.hub.dto.menu.MenuItemResponse;
import com.restaurant.hub.service.MenuItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "Menu")
public class MenuItemController {

    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @GetMapping("/api/menu")
    @Operation(summary = "Search and filter the menu (public)",
            description = "Supports restaurantId, categoryId, veg, search filters, plus standard page/size/sort params, e.g. ?sort=price,asc")
    public ResponseEntity<Page<MenuItemResponse>> search(
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean veg,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(menuItemService.search(restaurantId, categoryId, veg, search, pageable));
    }

    @GetMapping("/api/menu/{id}")
    @Operation(summary = "Get a single menu item (public)")
    public ResponseEntity<MenuItemResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(menuItemService.getById(id));
    }

    @PostMapping("/api/admin/menu")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Create a menu item")
    public ResponseEntity<MenuItemResponse> create(@Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuItemService.create(request));
    }

    @PutMapping("/api/admin/menu/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update a menu item")
    public ResponseEntity<MenuItemResponse> update(@PathVariable Long id, @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuItemService.update(id, request));
    }

    @PostMapping(value = "/api/admin/menu/{id}/image", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Upload a menu item image")
    public ResponseEntity<MenuItemResponse> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(menuItemService.updateImage(id, file));
    }

    @DeleteMapping("/api/admin/menu/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Delete a menu item")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        menuItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
