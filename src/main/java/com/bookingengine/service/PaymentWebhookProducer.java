package com.bookingengine.service;

import com.bookingengine.config.BookingEngineProperties;
import com.bookingengine.dto.WebhookPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Absorbs webhook bursts: the HTTP handler just appends to the stream and
 * returns immediately, while a consumer group processes entries at a
 * sustainable rate independent of arrival spikes.
 */
@Service
@RequiredArgsConstructor
public class PaymentWebhookProducer {

    private final StringRedisTemplate redisTemplate;
    private final BookingEngineProperties properties;

    public void publish(WebhookPayload payload) {
        Map<String, String> fields = Map.of(
                "eventId", payload.eventId(),
                "gatewayReference", payload.gatewayReference(),
                "status", payload.status()
        );
        MapRecord<String, String, String> record = StreamRecords.mapBacked(fields)
                .withStreamKey(properties.getWebhook().getStreamKey());
        redisTemplate.opsForStream().add(record);
    }
}
