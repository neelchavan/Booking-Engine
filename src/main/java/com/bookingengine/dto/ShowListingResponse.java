package com.bookingengine.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ShowListingResponse(
        Long showId,
        String movieTitle,
        String screenName,
        LocalDate showDate,
        LocalTime startTime,
        LocalTime endTime
) {
}
