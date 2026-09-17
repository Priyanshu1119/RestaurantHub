package com.restaurant.hub.service;

import com.restaurant.hub.dto.admin.StaffRequest;
import com.restaurant.hub.dto.admin.StaffResponse;
import com.restaurant.hub.entity.Restaurant;
import com.restaurant.hub.entity.User;
import com.restaurant.hub.enums.RoleName;
import com.restaurant.hub.exception.EmailAlreadyExistsException;
import com.restaurant.hub.exception.ResourceNotFoundException;
import com.restaurant.hub.exception.UnauthorizedException;
import com.restaurant.hub.repository.RestaurantRepository;
import com.restaurant.hub.repository.UserRepository;
import com.restaurant.hub.security.CurrentUserProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StaffService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserProvider currentUserProvider;

    public StaffService(UserRepository userRepository, RestaurantRepository restaurantRepository,
                         PasswordEncoder passwordEncoder, CurrentUserProvider currentUserProvider) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public StaffResponse createStaff(Long restaurantId, StaffRequest request) {
        assertCanManage(restaurantId);
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("An account with this email already exists");
        }
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id " + restaurantId));

        User staff = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(RoleName.RESTAURANT_STAFF)
                .restaurant(restaurant)
                .build();

        return StaffResponse.from(userRepository.save(staff));
    }

    public List<StaffResponse> listStaff(Long restaurantId) {
        assertCanManage(restaurantId);
        return userRepository.findByRestaurantIdAndRole(restaurantId, RoleName.RESTAURANT_STAFF)
                .stream().map(StaffResponse::from).toList();
    }

    @Transactional
    public void removeStaff(Long restaurantId, Long staffUserId) {
        assertCanManage(restaurantId);
        User staff = userRepository.findById(staffUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff account not found"));
        if (staff.getRestaurant() == null || !staff.getRestaurant().getId().equals(restaurantId)
                || staff.getRole() != RoleName.RESTAURANT_STAFF) {
            throw new UnauthorizedException("This account is not staff at this restaurant");
        }
        userRepository.delete(staff);
    }

    private void assertCanManage(Long restaurantId) {
        User current = currentUserProvider.getCurrentUser();
        if (current.getRole() == RoleName.SUPER_ADMIN) {
            return;
        }
        if (current.getRole() != RoleName.RESTAURANT_ADMIN
                || current.getRestaurant() == null
                || !current.getRestaurant().getId().equals(restaurantId)) {
            throw new UnauthorizedException("You do not have permission to manage staff for this restaurant");
        }
    }
}
