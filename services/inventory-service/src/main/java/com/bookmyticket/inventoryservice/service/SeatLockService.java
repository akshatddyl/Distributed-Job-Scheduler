package com.bookmyticket.inventoryservice.service;

import com.bookmyticket.inventoryservice.dto.LockSeatsRequest;
import com.bookmyticket.inventoryservice.entity.Seat;
import com.bookmyticket.inventoryservice.entity.SeatStatus;
import com.bookmyticket.inventoryservice.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatLockService {

    private final StringRedisTemplate redisTemplate;
    private final SeatRepository seatRepository;

    private static final String LOCK_PREFIX = "lock:seat:";
    private static final Duration LOCK_TTL = Duration.ofMinutes(5);

    @Transactional
    public void lockSeats(LockSeatsRequest request) {
        List<UUID> successfullyLockedRedisKeys = new ArrayList<>();
        
        try {
            // 1. Acquire Redis Locks
            for (UUID seatId : request.seatIds()) {
                String lockKey = LOCK_PREFIX + seatId;
                Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, request.userId().toString(), LOCK_TTL);
                
                if (Boolean.TRUE.equals(acquired)) {
                    successfullyLockedRedisKeys.add(seatId);
                } else {
                    throw new IllegalStateException("Seat " + seatId + " is currently locked by another user.");
                }
            }

            // 2. Fetch seats from DB and verify status
            List<Seat> seats = seatRepository.findAllById(request.seatIds());
            if (seats.size() != request.seatIds().size()) {
                throw new IllegalStateException("One or more seats not found in database.");
            }

            for (Seat seat : seats) {
                if (!seat.getEventId().equals(request.eventId())) {
                    throw new IllegalStateException("Seat " + seat.getId() + " does not belong to event " + request.eventId());
                }
                if (seat.getStatus() != SeatStatus.AVAILABLE) {
                    throw new IllegalStateException("Seat " + seat.getId() + " is no longer available (current status: " + seat.getStatus() + ").");
                }
                seat.setStatus(SeatStatus.LOCKED);
            }
            
            // 3. Save updated DB state
            seatRepository.saveAll(seats);
            log.info("Successfully locked {} seats for user {} and event {}", request.seatIds().size(), request.userId(), request.eventId());

        } catch (Exception e) {
            log.error("Failed to lock seats: {}. Rolling back acquired Redis locks.", e.getMessage());
            // Rollback any successfully acquired Redis locks before throwing
            for (UUID seatId : successfullyLockedRedisKeys) {
                redisTemplate.delete(LOCK_PREFIX + seatId);
            }
            throw new IllegalArgumentException("Failed to lock seats: " + e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "booking-events", groupId = "inventory-service-group")
    @Transactional
    public void handleBookingEvents(org.apache.kafka.clients.consumer.ConsumerRecord<String, Object> record) {
        Object value = record.value();
        
        if (value instanceof com.bookmyticket.shared.event.BookingConfirmedEvent event) {
            log.info("Received BookingConfirmedEvent for booking {}", event.bookingId());
            List<Seat> seats = seatRepository.findAllById(event.seatIds());
            for (Seat seat : seats) {
                seat.setStatus(SeatStatus.BOOKED);
                redisTemplate.delete(LOCK_PREFIX + seat.getId());
            }
            seatRepository.saveAll(seats);
        } else if (value instanceof com.bookmyticket.shared.event.BookingFailedEvent event) {
            log.info("Received BookingFailedEvent for booking {}", event.bookingId());
            List<Seat> seats = seatRepository.findAllById(event.seatIds());
            for (Seat seat : seats) {
                seat.setStatus(SeatStatus.AVAILABLE);
                redisTemplate.delete(LOCK_PREFIX + seat.getId());
            }
            seatRepository.saveAll(seats);
        }
    }
}
