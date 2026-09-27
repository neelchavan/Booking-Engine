package com.bookingengine.service;

import com.bookingengine.entity.Booking;
import com.bookingengine.entity.BookingStatus;
import com.bookingengine.repository.BookingRepository;
import com.bookingengine.repository.ShowSeatLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowSeatLockRepository showSeatLockRepository;

    /** No-op if already confirmed/cancelled/expired, so replayed webhooks are safe. */
    @Transactional
    public void confirm(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
        if (booking.getStatus() != BookingStatus.HOLD) {
            return;
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        showSeatLockRepository.confirmByBookingId(bookingId);
    }
}
