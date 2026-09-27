package com.bookingengine.service;

import com.bookingengine.config.BookingEngineProperties;
import com.bookingengine.entity.Booking;
import com.bookingengine.exception.SeatUnavailableException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Places a timed seat hold. Redis locks are the fast-path concurrency gate (fail
 * fast under contention); the show_seat_lock PRIMARY KEY is the durable guarantee
 * that survives even if Redis state is ever lost or stale.
 */
@Service
@RequiredArgsConstructor
public class SeatHoldService {

    private final RedissonClient redissonClient;
    private final BookingEngineProperties properties;
    private final HoldPersistenceService holdPersistenceService;

    private String lockKey(Long showId, Long seatId) {
        return "seat-lock:%d:%d".formatted(showId, seatId);
    }

    public Booking createHold(Long showId, Long customerId, List<Long> seatIds) {
        List<RLock> acquiredLocks = new ArrayList<>();
        int ttlSeconds = properties.getSeatHold().getTtlSeconds();
        try {
            for (Long seatId : seatIds) {
                RLock lock = redissonClient.getLock(lockKey(showId, seatId));
                boolean acquired;
                try {
                    acquired = lock.tryLock(0, ttlSeconds, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new SeatUnavailableException("Interrupted while acquiring seat lock for seat " + seatId);
                }
                if (!acquired) {
                    throw new SeatUnavailableException("Seat " + seatId + " is currently held by another booking");
                }
                acquiredLocks.add(lock);
            }
            Instant expiresAt = Instant.now().plus(ttlSeconds, ChronoUnit.SECONDS);
            return holdPersistenceService.persistHold(showId, customerId, seatIds, expiresAt);
        } catch (RuntimeException ex) {
            for (RLock lock : acquiredLocks) {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
            throw ex;
        }
    }
}
