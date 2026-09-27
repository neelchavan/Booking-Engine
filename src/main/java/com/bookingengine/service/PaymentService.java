package com.bookingengine.service;

import com.bookingengine.entity.Booking;
import com.bookingengine.entity.Payment;
import com.bookingengine.entity.PaymentStatus;
import com.bookingengine.entity.WebhookEvent;
import com.bookingengine.repository.BookingRepository;
import com.bookingengine.repository.PaymentRepository;
import com.bookingengine.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final WebhookEventRepository webhookEventRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    /** Mock gateway "charge created" step; a real webhook would arrive later confirming the outcome. */
    @Transactional
    public Payment createPendingPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setGatewayReference("pay_mock_" + UUID.randomUUID());
        payment.setStatus(PaymentStatus.PENDING);
        return paymentRepository.save(payment);
    }

    /**
     * Idempotent webhook processing: the INSERT into webhook_event is the guard —
     * a duplicate event_id fails it and this method returns false without
     * re-applying the payment/booking state change.
     */
    @Transactional
    public boolean processWebhookEvent(String eventId, String gatewayReference, String status, String rawPayload) {
        Optional<Payment> paymentOpt = paymentRepository.findByGatewayReference(gatewayReference);
        if (paymentOpt.isEmpty()) {
            log.warn("Webhook {} references unknown gateway_reference {}", eventId, gatewayReference);
            return false;
        }
        Payment payment = paymentOpt.get();

        WebhookEvent event = new WebhookEvent();
        event.setEventId(eventId);
        event.setPayment(payment);
        event.setPayload(rawPayload);
        try {
            webhookEventRepository.saveAndFlush(event);
        } catch (DataIntegrityViolationException duplicate) {
            log.info("Duplicate webhook event {} ignored", eventId);
            return false;
        }

        PaymentStatus newStatus = "SUCCESS".equalsIgnoreCase(status) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        payment.setStatus(newStatus);
        paymentRepository.save(payment);

        event.setProcessedAt(Instant.now());
        webhookEventRepository.save(event);

        if (newStatus == PaymentStatus.SUCCESS) {
            bookingService.confirm(payment.getBooking().getId());
        }
        return true;
    }
}
