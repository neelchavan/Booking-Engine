package com.bookingengine.dto;

import com.bookingengine.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BookingResponse(
        Long bookingId,
        BookingStatus status,
        List<Long> seatIds,
        BigDecimal totalAmount,
        Instant expiresAt
) {
}
