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
import org.springframework.kafka.core.KafkaTemplate;
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
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.inventory-service-url}")
    private String inventoryServiceUrl;

    @Value("${app.payment-service-url}")
    private String paymentServiceUrl;

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

    @Transactional
    public BookingResponse payForBooking(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Booking does not belong to user");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Booking is not in PENDING state");
        }

        List<UUID> seatIds = booking.getSeats().stream()
                .map(BookingSeat::getSeatId)
                .collect(Collectors.toList());

        Map<String, Object> paymentPayload = Map.of(
                "bookingId", bookingId,
                "userId", userId,
                "amount", booking.getTotalAmount()
        );

        boolean paymentSuccess = false;
        try {
            restClient.post()
                    .uri(paymentServiceUrl + "/api/payments/process")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(paymentPayload)
                    .retrieve()
                    .toBodilessEntity();
            paymentSuccess = true;
        } catch (RestClientResponseException e) {
            log.warn("Payment failed for booking {}: {}", bookingId, e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error communicating with payment-service", e);
        }

        if (paymentSuccess) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            com.bookmyticket.shared.event.BookingConfirmedEvent event = new com.bookmyticket.shared.event.BookingConfirmedEvent(
                    bookingId, booking.getEventId(), userId, seatIds);
            kafkaTemplate.send("booking-events", bookingId.toString(), event);
            log.info("Booking {} confirmed. Event published.", bookingId);
        } else {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            com.bookmyticket.shared.event.BookingFailedEvent event = new com.bookmyticket.shared.event.BookingFailedEvent(
                    bookingId, booking.getEventId(), userId, seatIds, "Payment Failed");
            kafkaTemplate.send("booking-events", bookingId.toString(), event);
            log.info("Booking {} cancelled. Event published.", bookingId);
        }

        return mapToResponse(booking);
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
