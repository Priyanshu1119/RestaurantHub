package com.restaurant.hub.repository;

import com.restaurant.hub.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Page<Ticket> findByUserId(Long userId, Pageable pageable);
    long countByEventIdAndStatus(Long eventId, com.restaurant.hub.enums.TicketStatus status);
}
