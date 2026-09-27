package com.bookingengine.repository;

import com.bookingengine.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByGatewayReference(String gatewayReference);
}
