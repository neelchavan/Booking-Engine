package com.bookingengine.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Live reservation state for a single (show, seat) pair. Primary key uniqueness
 * is what actually prevents double-booking at the database level.
 */
@Entity
@Table(name = "show_seat_lock")
@Getter
@Setter
@NoArgsConstructor
public class ShowSeatLock {

    @EmbeddedId
    private ShowSeatLockId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("showId")
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("seatId")
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LockStatus status;

    @Column(name = "expires_at")
    private Instant expiresAt;

    public ShowSeatLock(Show show, Seat seat, Long bookingId, LockStatus status, Instant expiresAt) {
        this.show = show;
        this.seat = seat;
        this.bookingId = bookingId;
        this.status = status;
        this.expiresAt = expiresAt;
        this.id = new ShowSeatLockId(show.getId(), seat.getId());
    }
}
