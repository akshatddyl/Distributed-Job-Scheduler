package com.bookmyticket.inventoryservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record LockSeatsRequest(
        @NotNull UUID eventId,
        @NotEmpty List<UUID> seatIds,
        @NotNull UUID userId
) {
}
