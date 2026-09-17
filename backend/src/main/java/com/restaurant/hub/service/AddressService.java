package com.restaurant.hub.service;

import com.restaurant.hub.dto.address.AddressRequest;
import com.restaurant.hub.dto.address.AddressResponse;
import com.restaurant.hub.entity.Address;
import com.restaurant.hub.entity.User;
import com.restaurant.hub.exception.ResourceNotFoundException;
import com.restaurant.hub.exception.UnauthorizedException;
import com.restaurant.hub.repository.AddressRepository;
import com.restaurant.hub.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final CurrentUserProvider currentUserProvider;

    public AddressService(AddressRepository addressRepository, CurrentUserProvider currentUserProvider) {
        this.addressRepository = addressRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public List<AddressResponse> listMine() {
        User current = currentUserProvider.getCurrentUser();
        return addressRepository.findByUserId(current.getId()).stream().map(AddressResponse::from).toList();
    }

    @Transactional
    public AddressResponse create(AddressRequest request) {
        User current = currentUserProvider.getCurrentUser();
        Address address = Address.builder()
                .user(current)
                .label(request.label())
                .line1(request.line1())
                .line2(request.line2())
                .city(request.city())
                .state(request.state())
                .postalCode(request.postalCode())
                .phone(request.phone())
                .build();
        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(Long id, AddressRequest request) {
        Address address = findOwnedOrThrow(id);
        address.setLabel(request.label());
        address.setLine1(request.line1());
        address.setLine2(request.line2());
        address.setCity(request.city());
        address.setState(request.state());
        address.setPostalCode(request.postalCode());
        address.setPhone(request.phone());
        return AddressResponse.from(address);
    }

    @Transactional
    public void delete(Long id) {
        addressRepository.delete(findOwnedOrThrow(id));
    }

    /** Also used internally by OrderService to fetch and validate ownership. */
    public Address findOwnedOrThrow(Long id) {
        User current = currentUserProvider.getCurrentUser();
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id " + id));
        if (!address.getUser().getId().equals(current.getId())) {
            throw new UnauthorizedException("You can only manage your own addresses");
        }
        return address;
    }
}
