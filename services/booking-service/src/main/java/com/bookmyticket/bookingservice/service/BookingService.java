package com.bookmyticket.bookingservice.service;

import com.bookmyticket.bookingservice.dto.BookingResponse;
import com.bookmyticket.bookingservice.dto.CreateBookingRequest;
import com.bookmyticket.bookingservice.entity.Booking;
import com.bookmyticket.bookingservice.entity.BookingSeat;
import com.bookmyticket.bookingservice.entity.BookingStatus;
import com.bookmyticket.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RestClient restClient;

    @Value("${app.inventory-service-url}")
    private String inventoryServiceUrl;

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, UUID userId) {
        log.info("Initiating booking for user {} on event {}", userId, request.eventId());

        // 1. Lock seats via inventory-service
        List<UUID> seatIds = request.seats().stream()
                .map(CreateBookingRequest.SeatInfo::seatId)
                .collect(Collectors.toList());

        Map<String, Object> lockPayload = Map.of(
                "eventId", request.eventId(),
                "seatIds", seatIds,
                "userId", userId
        );

        try {
            restClient.post()
                    .uri(inventoryServiceUrl + "/api/inventory/seats/lock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(lockPayload)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Successfully locked seats in inventory-service for user {}", userId);
        } catch (RestClientResponseException e) {
            log.error("Failed to lock seats in inventory-service: {}", e.getResponseBodyAsString());
            throw new IllegalStateException("Could not lock seats: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Error communicating with inventory-service", e);
            throw new IllegalStateException("Error communicating with inventory-service", e);
        }

        // 2. Create pending booking record
        Booking booking = Booking.builder()
                .userId(userId)
                .eventId(request.eventId())
                .totalAmount(request.totalAmount())
                .status(BookingStatus.PENDING)
                .build();

        for (CreateBookingRequest.SeatInfo seatInfo : request.seats()) {
            BookingSeat bookingSeat = BookingSeat.builder()
                    .seatId(seatInfo.seatId())
                    .price(seatInfo.price())
                    .build();
            booking.addSeat(bookingSeat);
        }

        booking = bookingRepository.save(booking);
        log.info("Created pending booking {} for user {}", booking.getId(), userId);

        return mapToResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(UUID userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BookingResponse mapToResponse(Booking booking) {
        List<BookingResponse.SeatResponse> seatResponses = booking.getSeats().stream()
                .map(s -> new BookingResponse.SeatResponse(s.getId(), s.getSeatId(), s.getPrice()))
                .collect(Collectors.toList());

        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getEventId(),
                booking.getTotalAmount(),
                booking.getStatus(),
                seatResponses,
                booking.getCreatedAt()
        );
    }
}
