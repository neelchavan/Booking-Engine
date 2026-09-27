package com.bookingengine.repository;

import com.bookingengine.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStatusAndExpiresAtBefore(com.bookingengine.entity.BookingStatus status, Instant cutoff);
}
