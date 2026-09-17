package com.restaurant.hub.service;

import com.restaurant.hub.dto.event.PurchaseTicketRequest;
import com.restaurant.hub.dto.event.TicketResponse;
import com.restaurant.hub.entity.Event;
import com.restaurant.hub.entity.Ticket;
import com.restaurant.hub.entity.User;
import com.restaurant.hub.enums.TicketStatus;
import com.restaurant.hub.exception.ResourceNotFoundException;
import com.restaurant.hub.exception.TicketUnavailableException;
import com.restaurant.hub.repository.EventRepository;
import com.restaurant.hub.repository.TicketRepository;
import com.restaurant.hub.security.CurrentUserProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final CurrentUserProvider currentUserProvider;
    private final NotificationService notificationService;

    public TicketService(TicketRepository ticketRepository, EventRepository eventRepository,
                          CurrentUserProvider currentUserProvider, NotificationService notificationService) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
        this.currentUserProvider = currentUserProvider;
        this.notificationService = notificationService;
    }

    /**
     * Reserves seats atomically before creating the ticket row, so two
     * concurrent purchases racing for the last seats can never both succeed.
     * If payment integration for tickets is added later, this is where a
     * ticket would start as RESERVED and move to PAID after verification,
     * the same pattern used for food orders in Phase 4.
     */
    @Transactional
    public TicketResponse purchase(Long eventId, PurchaseTicketRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id " + eventId));

        int updatedRows = eventRepository.reserveSeats(eventId, request.quantity());
        if (updatedRows == 0) {
            throw new TicketUnavailableException("Not enough seats remaining for this event");
        }

        User current = currentUserProvider.getCurrentUser();
        BigDecimal totalAmount = event.getTicketPrice().multiply(BigDecimal.valueOf(request.quantity()));

        Ticket ticket = Ticket.builder()
                .event(event)
                .user(current)
                .quantity(request.quantity())
                .totalAmount(totalAmount)
                .status(TicketStatus.PAID) // Simplified: full Razorpay wiring for tickets follows the Phase 4 pattern.
                .build();

        Ticket saved = ticketRepository.save(ticket);
        notificationService.notifyTicketPurchased(saved);
        return TicketResponse.from(saved);
    }

    public Page<TicketResponse> myTickets(Pageable pageable) {
        User current = currentUserProvider.getCurrentUser();
        return ticketRepository.findByUserId(current.getId(), pageable).map(TicketResponse::from);
    }

    @Transactional
    public void cancel(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id " + ticketId));
        User current = currentUserProvider.getCurrentUser();
        if (!ticket.getUser().getId().equals(current.getId())) {
            throw new com.restaurant.hub.exception.UnauthorizedException("This is not your ticket");
        }
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            return;
        }
        ticket.setStatus(TicketStatus.CANCELLED);
        eventRepository.releaseSeats(ticket.getEvent().getId(), ticket.getQuantity());
    }
}
