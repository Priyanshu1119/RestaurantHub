package com.restaurant.hub;

import com.restaurant.hub.dto.event.PurchaseTicketRequest;
import com.restaurant.hub.entity.Event;
import com.restaurant.hub.entity.Restaurant;
import com.restaurant.hub.entity.User;
import com.restaurant.hub.exception.TicketUnavailableException;
import com.restaurant.hub.repository.EventRepository;
import com.restaurant.hub.repository.TicketRepository;
import com.restaurant.hub.security.CurrentUserProvider;
import com.restaurant.hub.service.TicketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private EventRepository eventRepository;
    @Mock private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void purchase_rejectsWhenNotEnoughSeatsRemain() {
        Restaurant restaurant = Restaurant.builder().id(1L).name("Spice House").build();
        Event event = Event.builder().id(1L).restaurant(restaurant).name("Live Jazz Night")
                .eventDate(LocalDate.now().plusDays(7)).ticketPrice(BigDecimal.valueOf(500))
                .totalCapacity(50).remainingSeats(1).build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        // Atomic reservation reports 0 rows updated: someone else took the last seat.
        when(eventRepository.reserveSeats(1L, 5)).thenReturn(0);

        assertThrows(TicketUnavailableException.class,
                () -> ticketService.purchase(1L, new PurchaseTicketRequest(5)));
    }
}
