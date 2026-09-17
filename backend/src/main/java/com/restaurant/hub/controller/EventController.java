package com.restaurant.hub.controller;

import com.restaurant.hub.dto.event.EventRequest;
import com.restaurant.hub.dto.event.EventResponse;
import com.restaurant.hub.service.EventService;
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
@Tag(name = "Events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/api/events")
    @Operation(summary = "Browse events for a restaurant (public)")
    public ResponseEntity<Page<EventResponse>> list(@RequestParam Long restaurantId, Pageable pageable) {
        return ResponseEntity.ok(eventService.listByRestaurant(restaurantId, pageable));
    }

    @GetMapping("/api/events/{id}")
    @Operation(summary = "Get one event (public)")
    public ResponseEntity<EventResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getById(id));
    }

    @PostMapping("/api/admin/events")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Create an event")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(request));
    }

    @PutMapping("/api/admin/events/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update an event")
    public ResponseEntity<EventResponse> update(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.update(id, request));
    }

    @PostMapping(value = "/api/admin/events/{id}/image", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Upload an event image")
    public ResponseEntity<EventResponse> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(eventService.uploadImage(id, file));
    }

    @DeleteMapping("/api/admin/events/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Delete an event")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
