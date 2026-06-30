package com.bookmyticket.shared.event;

import java.util.List;
import java.util.UUID;

public record BookingFailedEvent(
        UUID bookingId,
        UUID eventId,
        UUID userId,
        List<UUID> seatIds,
        String reason
) {
}
