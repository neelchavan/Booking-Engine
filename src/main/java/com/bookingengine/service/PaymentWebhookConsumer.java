package com.bookingengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentWebhookConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final PaymentService paymentService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final String streamKey;
    private final String consumerGroup;

    public PaymentWebhookConsumer(PaymentService paymentService,
                                   StringRedisTemplate redisTemplate,
                                   ObjectMapper objectMapper,
                                   com.bookingengine.config.BookingEngineProperties properties) {
        this.paymentService = paymentService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.streamKey = properties.getWebhook().getStreamKey();
        this.consumerGroup = properties.getWebhook().getConsumerGroup();
    }

    @Override
    public void onMessage(MapRecord<String, String, String> record) {
        try {
            var fields = record.getValue();
            String eventId = fields.get("eventId");
            String gatewayReference = fields.get("gatewayReference");
            String status = fields.get("status");
            String rawPayload = objectMapper.writeValueAsString(fields);

            paymentService.processWebhookEvent(eventId, gatewayReference, status, rawPayload);
        } catch (Exception ex) {
            log.error("Failed to process webhook stream record {}", record.getId(), ex);
            // Left un-acked; a redelivery/claim mechanism can retry it later.
            return;
        }
        redisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, record.getId());
    }
}
