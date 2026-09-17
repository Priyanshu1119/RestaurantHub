package com.restaurant.hub.dto.address;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        String label,

        @NotBlank(message = "Address line 1 is required")
        String line1,

        String line2,

        @NotBlank(message = "City is required")
        String city,

        String state,
        String postalCode,

        @NotBlank(message = "Phone is required")
        String phone
) {}
