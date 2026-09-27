package com.bookingengine.dto;

public record WebhookPayload(
        String eventId,
        String gatewayReference,
        String status
) {
}
