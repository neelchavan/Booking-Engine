package com.bookingengine.controller;

import com.bookingengine.entity.Payment;
import com.bookingengine.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Stands in for a real payment gateway's "create charge" API. */
@RestController
@RequestMapping("/api/bookings/{bookingId}/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> charge(@PathVariable Long bookingId) {
        Payment payment = paymentService.createPendingPayment(bookingId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "paymentId", payment.getId(),
                "gatewayReference", payment.getGatewayReference(),
                "status", payment.getStatus()
        ));
    }
}
