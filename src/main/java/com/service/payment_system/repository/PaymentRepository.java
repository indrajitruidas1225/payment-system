package com.service.payment_system.repository;

import com.service.payment_system.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByIdempotencyKey(String key);
}
