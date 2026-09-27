package com.bookingengine.controller;

import com.bookingengine.dto.WebhookPayload;
import com.bookingengine.service.PaymentWebhookProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Mock payment gateway webhook receiver. Bursts of deliveries (including
 * gateway retries with a repeated eventId) are absorbed by immediately
 * queuing to Redis and returning, instead of processing inline.
 */
@RestController
@RequestMapping("/api/webhooks/payment")
@RequiredArgsConstructor
public class WebhookController {

    private final PaymentWebhookProducer producer;

    @PostMapping
    public ResponseEntity<Void> receive(@Valid @RequestBody WebhookPayload payload) {
        producer.publish(payload);
        return ResponseEntity.accepted().build();
    }
}
