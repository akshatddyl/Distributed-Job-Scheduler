package com.bookmyticket.notificationservice.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationListener {

    @KafkaListener(topics = "booking-events", groupId = "notification-service-group")
    public void handleBookingEvents(org.apache.kafka.clients.consumer.ConsumerRecord<String, Object> record) {
        Object value = record.value();

        if (value instanceof com.bookmyticket.shared.event.BookingConfirmedEvent event) {
            log.info("=================================================");
            log.info("SIMULATED EMAIL NOTIFICATION");
            log.info("To User: {}", event.userId());
            log.info("Subject: Your Booking is Confirmed! (Booking ID: {})", event.bookingId());
            log.info("Details: Event ID {}, Seats: {}", event.eventId(), event.seatIds());
            log.info("=================================================");
        } else if (value instanceof com.bookmyticket.shared.event.BookingFailedEvent event) {
            log.info("=================================================");
            log.info("SIMULATED EMAIL NOTIFICATION");
            log.info("To User: {}", event.userId());
            log.info("Subject: Your Booking Failed (Booking ID: {})", event.bookingId());
            log.info("Reason: {}", event.reason());
            log.info("=================================================");
        }
    }
}
