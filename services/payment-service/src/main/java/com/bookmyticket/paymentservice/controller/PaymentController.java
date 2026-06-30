package com.bookmyticket.paymentservice.controller;

import com.bookmyticket.paymentservice.dto.ProcessPaymentRequest;
import com.bookmyticket.shared.dto.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@Slf4j
public class PaymentController {

    @PostMapping("/process")
    public ResponseEntity<ResponseWrapper<Void>> processPayment(@Valid @RequestBody ProcessPaymentRequest request) {
        log.info("Processing payment for booking {} and user {} with amount {}", 
                request.bookingId(), request.userId(), request.amount());

        // Simulate 90% success rate
        boolean success = Math.random() < 0.90;

        if (success) {
            log.info("Payment SUCCESS for booking {}", request.bookingId());
            return ResponseEntity.ok(ResponseWrapper.success("Payment processed successfully", null));
        } else {
            log.warn("Payment FAILED for booking {}", request.bookingId());
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(ResponseWrapper.failure("Payment failed due to insufficient funds or bank error"));
        }
    }
}
