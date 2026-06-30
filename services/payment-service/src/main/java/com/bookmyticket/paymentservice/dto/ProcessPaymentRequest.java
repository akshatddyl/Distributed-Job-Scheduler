package com.bookmyticket.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentRequest(
        @NotNull UUID bookingId,
        @NotNull UUID userId,
        @NotNull @DecimalMin("0.01") BigDecimal amount
) {
}
