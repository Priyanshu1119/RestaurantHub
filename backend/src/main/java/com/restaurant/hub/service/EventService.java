package com.restaurant.hub.service;

import com.restaurant.hub.dto.event.EventRequest;
import com.restaurant.hub.dto.event.EventResponse;
import com.restaurant.hub.entity.Event;
import com.restaurant.hub.entity.Restaurant;
import com.restaurant.hub.entity.User;
import com.restaurant.hub.enums.RoleName;
import com.restaurant.hub.exception.ResourceNotFoundException;
import com.restaurant.hub.exception.UnauthorizedException;
import com.restaurant.hub.repository.EventRepository;
import com.restaurant.hub.repository.RestaurantRepository;
import com.restaurant.hub.security.CurrentUserProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final RestaurantRepository restaurantRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ImageStorageService imageStorageService;

    public EventService(EventRepository eventRepository, RestaurantRepository restaurantRepository,
                         CurrentUserProvider currentUserProvider, ImageStorageService imageStorageService) {
        this.eventRepository = eventRepository;
        this.restaurantRepository = restaurantRepository;
        this.currentUserProvider = currentUserProvider;
        this.imageStorageService = imageStorageService;
    }

    public Page<EventResponse> listByRestaurant(Long restaurantId, Pageable pageable) {
        return eventRepository.findByRestaurantId(restaurantId, pageable).map(EventResponse::from);
    }

    public EventResponse getById(Long id) {
        return EventResponse.from(findOrThrow(id));
    }

    @Transactional
    public EventResponse create(EventRequest request) {
        Long restaurantId = resolveRestaurantId(request.restaurantId());
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id " + restaurantId));

        Event event = Event.builder()
                .restaurant(restaurant)
                .name(request.name())
                .description(request.description())
                .eventDate(request.eventDate())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .location(request.location())
                .ticketPrice(request.ticketPrice())
                .totalCapacity(request.totalCapacity())
                .remainingSeats(request.totalCapacity())
                .build();

        return EventResponse.from(eventRepository.save(event));
    }

    @Transactional
    public EventResponse update(Long id, EventRequest request) {
        Event event = findOrThrow(id);
        assertCanManage(event.getRestaurant().getId());

        // Capacity can only grow here; shrinking below tickets already sold
        // would require a refund flow that's out of scope for this phase.
        int seatsSold = event.getTotalCapacity() - event.getRemainingSeats();
        int newCapacity = Math.max(request.totalCapacity(), seatsSold);

        event.setName(request.name());
        event.setDescription(request.description());
        event.setEventDate(request.eventDate());
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());
        event.setLocation(request.location());
        event.setTicketPrice(request.ticketPrice());
        event.setRemainingSeats(event.getRemainingSeats() + (newCapacity - event.getTotalCapacity()));
        event.setTotalCapacity(newCapacity);

        return EventResponse.from(event);
    }

    @Transactional
    public EventResponse uploadImage(Long id, MultipartFile file) {
        Event event = findOrThrow(id);
        assertCanManage(event.getRestaurant().getId());
        event.setImageUrl(imageStorageService.upload(file, "events"));
        return EventResponse.from(event);
    }

    @Transactional
    public void delete(Long id) {
        Event event = findOrThrow(id);
        assertCanManage(event.getRestaurant().getId());
        eventRepository.delete(event);
    }

    private Event findOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id " + id));
    }

    private Long resolveRestaurantId(Long requestedRestaurantId) {
        User current = currentUserProvider.getCurrentUser();
        if (current.getRole() == RoleName.SUPER_ADMIN) {
            if (requestedRestaurantId == null) {
                throw new IllegalArgumentException("restaurantId is required for super admin requests");
            }
            return requestedRestaurantId;
        }
        if (current.getRestaurant() == null) {
            throw new UnauthorizedException("Your account is not linked to a restaurant");
        }
        return current.getRestaurant().getId();
    }

    private void assertCanManage(Long restaurantId) {
        User current = currentUserProvider.getCurrentUser();
        if (current.getRole() == RoleName.SUPER_ADMIN) {
            return;
        }
        if (current.getRestaurant() == null || !current.getRestaurant().getId().equals(restaurantId)) {
            throw new UnauthorizedException("You do not have permission to manage this event");
        }
    }
}
