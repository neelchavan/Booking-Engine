package com.bookingengine.repository;

import com.bookingengine.entity.LockStatus;
import com.bookingengine.entity.ShowSeatLock;
import com.bookingengine.entity.ShowSeatLockId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ShowSeatLockRepository extends JpaRepository<ShowSeatLock, ShowSeatLockId> {

    List<ShowSeatLock> findByStatusAndExpiresAtBefore(LockStatus status, Instant cutoff);

    @Modifying
    @Query("UPDATE ShowSeatLock l SET l.status = 'CONFIRMED', l.expiresAt = null WHERE l.bookingId = :bookingId")
    int confirmByBookingId(@Param("bookingId") Long bookingId);

    @Modifying
    @Query("DELETE FROM ShowSeatLock l WHERE l.bookingId = :bookingId")
    int deleteByBookingId(@Param("bookingId") Long bookingId);
}
