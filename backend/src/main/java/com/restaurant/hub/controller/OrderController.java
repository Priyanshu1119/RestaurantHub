package com.restaurant.hub.controller;

import com.restaurant.hub.dto.order.CreateOrderRequest;
import com.restaurant.hub.dto.order.OrderResponse;
import com.restaurant.hub.dto.order.OrderStatusUpdateRequest;
import com.restaurant.hub.enums.OrderStatus;
import com.restaurant.hub.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/api/orders")
    @Operation(summary = "Place an order from the current cart")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createFromCart(request));
    }

    @GetMapping("/api/orders")
    @Operation(summary = "List the logged-in customer's own orders")
    public ResponseEntity<Page<OrderResponse>> myOrders(Pageable pageable) {
        return ResponseEntity.ok(orderService.myOrders(pageable));
    }

    @GetMapping("/api/orders/{id}")
    @Operation(summary = "Get one order (owner, restaurant staff, or super admin only)")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @PatchMapping("/api/orders/{id}/status")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'RESTAURANT_STAFF', 'SUPER_ADMIN')")
    @Operation(summary = "Move an order to its next valid status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                        @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request.status()));
    }

    @PostMapping("/api/orders/{id}/cancel")
    @Operation(summary = "Cancel your own order while it's still PENDING or CONFIRMED")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOwnOrder(id));
    }

    @GetMapping("/api/admin/orders")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'RESTAURANT_STAFF', 'SUPER_ADMIN')")
    @Operation(summary = "List orders for a restaurant, optionally filtered by status")
    public ResponseEntity<Page<OrderResponse>> restaurantOrders(
            @RequestParam Long restaurantId,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.restaurantOrders(restaurantId, status, pageable));
    }
}
