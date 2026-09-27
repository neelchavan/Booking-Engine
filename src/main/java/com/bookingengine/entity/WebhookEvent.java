package com.bookingengine.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "webhook_event")
@Getter
@Setter
@NoArgsConstructor
public class WebhookEvent {

    @Id
    @Column(name = "event_id", length = 100)
    private String eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false, columnDefinition = "json")
    private String payload;

    @Column(name = "received_at", insertable = false, updatable = false)
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;
}
