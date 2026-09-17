package com.restaurant.hub.controller;

import com.restaurant.hub.dto.cart.CartItemRequest;
import com.restaurant.hub.dto.cart.CartResponse;
import com.restaurant.hub.dto.cart.UpdateQuantityRequest;
import com.restaurant.hub.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(summary = "Get the logged-in customer's cart")
    public ResponseEntity<CartResponse> get() {
        return ResponseEntity.ok(cartService.getMyCart());
    }

    @PostMapping("/items")
    @Operation(summary = "Add an item to the cart")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(request));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update a cart item's quantity")
    public ResponseEntity<CartResponse> updateQuantity(@PathVariable Long itemId,
                                                         @Valid @RequestBody UpdateQuantityRequest request) {
        return ResponseEntity.ok(cartService.updateQuantity(itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove an item from the cart")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(itemId));
    }

    @DeleteMapping
    @Operation(summary = "Clear the entire cart")
    public ResponseEntity<Void> clear() {
        cartService.clear();
        return ResponseEntity.noContent().build();
    }
}
