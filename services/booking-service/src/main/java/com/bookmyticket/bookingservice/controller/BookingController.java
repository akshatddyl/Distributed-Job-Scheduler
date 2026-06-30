package com.bookmyticket.bookingservice.controller;

import com.bookmyticket.bookingservice.dto.BookingResponse;
import com.bookmyticket.bookingservice.dto.CreateBookingRequest;
import com.bookmyticket.bookingservice.service.BookingService;
import com.bookmyticket.shared.dto.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseWrapper<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        BookingResponse response = bookingService.createBooking(request, userId);
        return ResponseWrapper.success("Booking created successfully", response);
    }

    @GetMapping("/me")
    public ResponseWrapper<List<BookingResponse>> getMyBookings(@RequestHeader("X-User-Id") UUID userId) {
        List<BookingResponse> responses = bookingService.getUserBookings(userId);
        return ResponseWrapper.success("Bookings retrieved successfully", responses);
    }
}
