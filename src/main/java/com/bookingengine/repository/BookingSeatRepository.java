package com.bookingengine.repository;

import com.bookingengine.entity.BookingSeat;
import com.bookingengine.entity.BookingSeatId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, BookingSeatId> {
    List<BookingSeat> findByIdBookingId(Long bookingId);
}
