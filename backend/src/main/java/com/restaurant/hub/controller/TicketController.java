package com.restaurant.hub.controller;

import com.restaurant.hub.dto.event.PurchaseTicketRequest;
import com.restaurant.hub.dto.event.TicketResponse;
import com.restaurant.hub.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/api/events/{id}/tickets")
    @Operation(summary = "Purchase tickets for an event")
    public ResponseEntity<TicketResponse> purchase(@PathVariable Long id, @Valid @RequestBody PurchaseTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.purchase(id, request));
    }

    @GetMapping("/api/tickets")
    @Operation(summary = "List the logged-in customer's ticket purchases")
    public ResponseEntity<Page<TicketResponse>> myTickets(Pageable pageable) {
        return ResponseEntity.ok(ticketService.myTickets(pageable));
    }

    @DeleteMapping("/api/tickets/{id}")
    @Operation(summary = "Cancel a ticket and release its seats")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        ticketService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
