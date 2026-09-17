package com.restaurant.hub.controller;

import com.restaurant.hub.dto.marketing.AssetResponse;
import com.restaurant.hub.service.MarketingAssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/marketing/assets")
@PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'SUPER_ADMIN')")
@Tag(name = "Marketing Assets")
public class MarketingAssetController {

    private final MarketingAssetService assetService;

    public MarketingAssetController(MarketingAssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    @Operation(summary = "List downloadable marketing assets for a restaurant")
    public ResponseEntity<List<AssetResponse>> list(@RequestParam Long restaurantId) {
        return ResponseEntity.ok(assetService.listByRestaurant(restaurantId));
    }

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Upload a marketing asset (business card, decal, banner, etc)")
    public ResponseEntity<AssetResponse> upload(@RequestParam Long restaurantId,
                                                 @RequestParam String name,
                                                 @RequestParam String assetType,
                                                 @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assetService.upload(restaurantId, name, assetType, file));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a marketing asset")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
