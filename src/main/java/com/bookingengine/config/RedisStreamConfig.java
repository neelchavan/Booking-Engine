package com.bookingengine.config;

import com.bookingengine.service.PaymentWebhookConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisStreamConfig {

    private final BookingEngineProperties properties;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> paymentWebhookListenerContainer(
            RedisConnectionFactory connectionFactory,
            StringRedisTemplate redisTemplate,
            PaymentWebhookConsumer consumer) {

        String streamKey = properties.getWebhook().getStreamKey();
        String groupName = properties.getWebhook().getConsumerGroup();
        ensureStreamAndGroupExist(redisTemplate, streamKey, groupName);

        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofSeconds(2))
                        .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(connectionFactory, options);

        container.receive(
                Consumer.from(groupName, "consumer-1"),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                consumer);

        return container;
    }

    private void ensureStreamAndGroupExist(StringRedisTemplate redisTemplate, String streamKey, String groupName) {
        try {
            redisTemplate.opsForStream().createGroup(streamKey, groupName);
        } catch (Exception ex) {
            // BUSYGROUP: group already exists from a previous run - safe to ignore.
            log.debug("Consumer group {} on stream {} already exists", groupName, streamKey);
        }
    }
}
