package com.bookingengine.service;

import com.bookingengine.entity.*;
import com.bookingengine.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Isolated so the DB writes run inside a real Spring-managed transaction (not a self-invocation). */
@Service
@RequiredArgsConstructor
class HoldPersistenceService {

    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowSeatLockRepository showSeatLockRepository;

    @Transactional
    Booking persistHold(Long showId, Long customerId, List<Long> seatIds, Instant expiresAt) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new IllegalArgumentException("Show not found: " + showId));
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        List<Seat> seats = seatRepository.findByIdIn(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new IllegalArgumentException("One or more seat ids do not exist");
        }

        BigDecimal total = seats.stream().map(Seat::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

        Booking booking = new Booking();
        booking.setShow(show);
        booking.setCustomer(customer);
        booking.setStatus(BookingStatus.HOLD);
        booking.setTotalAmount(total);
        booking.setExpiresAt(expiresAt);
        booking = bookingRepository.save(booking);

        // Each insert below fails with a duplicate-key error if the seat is already
        // locked/booked for this show, guaranteeing no double-booking even if the
        // Redis lock was somehow bypassed or stale.
        for (Seat seat : seats) {
            bookingSeatRepository.save(new BookingSeat(booking, seat, seat.getPrice()));
            showSeatLockRepository.save(new ShowSeatLock(show, seat, booking.getId(), LockStatus.HOLD, expiresAt));
        }
        return booking;
    }
}
