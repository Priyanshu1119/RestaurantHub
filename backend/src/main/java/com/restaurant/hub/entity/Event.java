package com.restaurant.hub.entity;

import com.restaurant.hub.enums.EventStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_events_restaurant", columnList = "restaurant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDate eventDate;

    private LocalTime startTime;
    private LocalTime endTime;

    @Column(length = 250)
    private String location;

    private String imageUrl;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal ticketPrice;

    @Column(nullable = false)
    private Integer totalCapacity;

    @Column(nullable = false)
    private Integer remainingSeats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EventStatus status = EventStatus.UPCOMING;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (remainingSeats == null) remainingSeats = totalCapacity;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
