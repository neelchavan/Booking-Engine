package com.bookingengine.service;

import com.bookingengine.entity.Booking;
import com.bookingengine.entity.BookingStatus;
import com.bookingengine.entity.LockStatus;
import com.bookingengine.entity.ShowSeatLock;
import com.bookingengine.repository.BookingRepository;
import com.bookingengine.repository.ShowSeatLockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Backstop for expired holds: the Redis lock TTL already frees the seat for new
 * requests, but this job clears the corresponding DB rows so `show_seat_lock`
 * doesn't keep a seat marked as held forever if the DB write happened but the
 * app crashed before a natural follow-up, or Redis state was lost.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HoldExpiryReconciliationService {

    private final ShowSeatLockRepository showSeatLockRepository;
    private final BookingRepository bookingRepository;

    @Scheduled(fixedDelayString = "PT30S")
    @Transactional
    public void releaseExpiredHolds() {
        Instant now = Instant.now();
        List<ShowSeatLock> expiredLocks = showSeatLockRepository.findByStatusAndExpiresAtBefore(LockStatus.HOLD, now);
        for (ShowSeatLock lock : expiredLocks) {
            showSeatLockRepository.delete(lock);
        }

        List<Booking> expiredHolds = bookingRepository.findByStatusAndExpiresAtBefore(BookingStatus.HOLD, now);
        for (Booking booking : expiredHolds) {
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
        }
        if (!expiredLocks.isEmpty() || !expiredHolds.isEmpty()) {
            log.info("Released {} expired seat lock(s), expired {} booking(s)", expiredLocks.size(), expiredHolds.size());
        }
    }
}
