package com.restaurant.hub.controller;

import com.restaurant.hub.dto.address.AddressRequest;
import com.restaurant.hub.dto.address.AddressResponse;
import com.restaurant.hub.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@Tag(name = "Addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    @Operation(summary = "List the logged-in customer's saved addresses")
    public ResponseEntity<List<AddressResponse>> list() {
        return ResponseEntity.ok(addressService.listMine());
    }

    @PostMapping
    @Operation(summary = "Add a delivery address")
    public ResponseEntity<AddressResponse> create(@Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a delivery address")
    public ResponseEntity<AddressResponse> update(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a delivery address")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
