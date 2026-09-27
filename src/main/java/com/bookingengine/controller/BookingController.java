package com.bookingengine.controller;

import com.bookingengine.dto.BookingResponse;
import com.bookingengine.dto.CreateHoldRequest;
import com.bookingengine.entity.Booking;
import com.bookingengine.entity.BookingSeat;
import com.bookingengine.entity.Seat;
import com.bookingengine.repository.BookingSeatRepository;
import com.bookingengine.service.SeatHoldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final SeatHoldService seatHoldService;
    private final BookingSeatRepository bookingSeatRepository;

    @PostMapping
    public ResponseEntity<BookingResponse> createHold(@Valid @RequestBody CreateHoldRequest request) {
        Booking booking = seatHoldService.createHold(request.showId(), request.customerId(), request.seatIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(booking));
    }

    private BookingResponse toResponse(Booking booking) {
        var seatIds = bookingSeatRepository.findByIdBookingId(booking.getId()).stream()
                .map(BookingSeat::getSeat)
                .map(Seat::getId)
                .toList();
        return new BookingResponse(booking.getId(), booking.getStatus(), seatIds, booking.getTotalAmount(), booking.getExpiresAt());
    }
}
