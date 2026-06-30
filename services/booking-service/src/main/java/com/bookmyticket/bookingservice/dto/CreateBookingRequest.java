package com.bookmyticket.bookingservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateBookingRequest(
        @NotNull UUID eventId,
        @NotEmpty List<SeatInfo> seats,
        @NotNull @DecimalMin("0.01") BigDecimal totalAmount
) {
    public record SeatInfo(@NotNull UUID seatId, @NotNull @DecimalMin("0.01") BigDecimal price) {}
}
