package com.bookingengine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "booking-engine")
public class BookingEngineProperties {

    private final SeatHold seatHold = new SeatHold();
    private final Webhook webhook = new Webhook();

    public SeatHold getSeatHold() {
        return seatHold;
    }

    public Webhook getWebhook() {
        return webhook;
    }

    public static class SeatHold {
        private int ttlSeconds = 300;

        public int getTtlSeconds() {
            return ttlSeconds;
        }

        public void setTtlSeconds(int ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
        }
    }

    public static class Webhook {
        private String streamKey = "payment-webhooks";
        private String consumerGroup = "payment-webhooks-group";

        public String getStreamKey() {
            return streamKey;
        }

        public void setStreamKey(String streamKey) {
            this.streamKey = streamKey;
        }

        public String getConsumerGroup() {
            return consumerGroup;
        }

        public void setConsumerGroup(String consumerGroup) {
            this.consumerGroup = consumerGroup;
        }
    }
}
