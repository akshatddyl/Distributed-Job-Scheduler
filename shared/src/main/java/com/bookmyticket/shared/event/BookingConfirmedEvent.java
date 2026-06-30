package com.bookmyticket.shared.event;

import java.util.List;
import java.util.UUID;

public record BookingConfirmedEvent(
        UUID bookingId,
        UUID eventId,
        UUID userId,
        List<UUID> seatIds
) {
}
