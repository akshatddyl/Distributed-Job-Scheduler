package com.bookmyticket.inventoryservice.controller;

import com.bookmyticket.inventoryservice.dto.CreateSeatsRequest;
import com.bookmyticket.inventoryservice.dto.SeatAvailabilityResponse;
import com.bookmyticket.inventoryservice.dto.SeatResponse;
import com.bookmyticket.inventoryservice.service.InventoryService;
import com.bookmyticket.shared.dto.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final com.bookmyticket.inventoryservice.service.SeatLockService seatLockService;

    @PostMapping("/seats")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseWrapper<List<SeatResponse>> initializeSeats(@Valid @RequestBody CreateSeatsRequest request) {
        List<SeatResponse> response = inventoryService.initializeSeats(request);
        return ResponseWrapper.success("Seats initialized successfully", response);
    }

    @GetMapping("/seats")
    public ResponseWrapper<List<SeatResponse>> getSeatsByEvent(@RequestParam UUID eventId) {
        return ResponseWrapper.success("Seats retrieved successfully", inventoryService.getSeatsByEvent(eventId));
    }

    @GetMapping("/availability")
    public ResponseWrapper<SeatAvailabilityResponse> getAvailability(@RequestParam UUID eventId) {
        return ResponseWrapper.success("Availability retrieved successfully", inventoryService.getAvailability(eventId));
    }

    @GetMapping("/seats/available")
    public ResponseWrapper<List<SeatResponse>> getAvailableSeats(@RequestParam UUID eventId) {
        return ResponseWrapper.success("Available seats retrieved successfully", inventoryService.getAvailableSeats(eventId));
    }

    @PostMapping("/seats/lock")
    public ResponseWrapper<Void> lockSeats(@Valid @RequestBody com.bookmyticket.inventoryservice.dto.LockSeatsRequest request) {
        seatLockService.lockSeats(request);
        return ResponseWrapper.success("Seats locked successfully", null);
    }
}
