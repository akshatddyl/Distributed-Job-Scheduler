package com.bookmyticket.userservice.dto;

import java.util.UUID;

/**
 * Returned after successful registration or login.
 */
public record AuthResponse(
        UUID userId,
        String email,
        String role,
        String token
) {}
