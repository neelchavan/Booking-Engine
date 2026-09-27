package com.bookingengine.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateHoldRequest(
        @NotNull Long showId,
        @NotNull Long customerId,
        @NotEmpty List<Long> seatIds
) {
}
