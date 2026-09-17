package com.restaurant.hub.controller;

import com.restaurant.hub.dto.marketing.CampaignRequest;
import com.restaurant.hub.dto.marketing.CampaignResponse;
import com.restaurant.hub.service.MarketingCampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/marketing/campaigns")
@PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'SUPER_ADMIN')")
@Tag(name = "Marketing Campaigns")
public class MarketingCampaignController {

    private final MarketingCampaignService campaignService;

    public MarketingCampaignController(MarketingCampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping
    @Operation(summary = "List campaigns for a restaurant")
    public ResponseEntity<List<CampaignResponse>> list(@RequestParam Long restaurantId) {
        return ResponseEntity.ok(campaignService.listByRestaurant(restaurantId));
    }

    @PostMapping
    @Operation(summary = "Create an email campaign (draft or scheduled)")
    public ResponseEntity<CampaignResponse> create(@Valid @RequestBody CampaignRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campaignService.create(request));
    }

    @PostMapping("/{id}/send")
    @Operation(summary = "Mark a campaign as sent")
    public ResponseEntity<CampaignResponse> send(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.markSent(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a campaign")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        campaignService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
