package com.bookmyticket.bookingservice.dto;

import com.bookmyticket.bookingservice.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID userId,
        UUID eventId,
        BigDecimal totalAmount,
        BookingStatus status,
        List<SeatResponse> seats,
        Instant createdAt
) {
    public record SeatResponse(UUID id, UUID seatId, BigDecimal price) {}
}
