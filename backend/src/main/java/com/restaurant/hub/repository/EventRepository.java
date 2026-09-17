package com.restaurant.hub.repository;

import com.restaurant.hub.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByRestaurantId(Long restaurantId, Pageable pageable);

    /**
     * Atomic, race-safe seat reservation. Only decrements when enough seats
     * remain, so two concurrent purchases can never oversell the same event.
     * Returns the number of rows updated: 1 on success, 0 if not enough seats.
     */
    @Modifying
    @Query("UPDATE Event e SET e.remainingSeats = e.remainingSeats - :quantity " +
            "WHERE e.id = :eventId AND e.remainingSeats >= :quantity")
    int reserveSeats(@Param("eventId") Long eventId, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE Event e SET e.remainingSeats = e.remainingSeats + :quantity WHERE e.id = :eventId")
    void releaseSeats(@Param("eventId") Long eventId, @Param("quantity") int quantity);
}
