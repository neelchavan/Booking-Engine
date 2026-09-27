package com.bookingengine.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "booking_seat")
@Getter
@Setter
@NoArgsConstructor
public class BookingSeat {

    @EmbeddedId
    private BookingSeatId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookingId")
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("seatId")
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "price_at_booking", nullable = false, precision = 8, scale = 2)
    private BigDecimal priceAtBooking;

    public BookingSeat(Booking booking, Seat seat, BigDecimal priceAtBooking) {
        this.booking = booking;
        this.seat = seat;
        this.priceAtBooking = priceAtBooking;
        this.id = new BookingSeatId(booking.getId(), seat.getId());
    }
}
