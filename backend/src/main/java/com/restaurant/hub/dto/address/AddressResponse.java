package com.restaurant.hub.dto.address;

import com.restaurant.hub.entity.Address;

public record AddressResponse(
        Long id,
        String label,
        String line1,
        String line2,
        String city,
        String state,
        String postalCode,
        String phone
) {
    public static AddressResponse from(Address a) {
        return new AddressResponse(a.getId(), a.getLabel(), a.getLine1(), a.getLine2(),
                a.getCity(), a.getState(), a.getPostalCode(), a.getPhone());
    }

    public String toSingleLine() {
        StringBuilder sb = new StringBuilder();
        sb.append(line1());
        if (line2() != null && !line2().isBlank()) sb.append(", ").append(line2());
        sb.append(", ").append(city());
        if (state() != null && !state().isBlank()) sb.append(", ").append(state());
        if (postalCode() != null && !postalCode().isBlank()) sb.append(" ").append(postalCode());
        return sb.toString();
    }
}
