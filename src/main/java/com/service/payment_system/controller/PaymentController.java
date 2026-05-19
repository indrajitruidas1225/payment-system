package com.service.payment_system.controller;

import com.service.payment_system.dto.PaymentRequest;
import com.service.payment_system.entity.Payment;
import com.service.payment_system.event.PaymentEvent;
import com.service.payment_system.producer.PaymentProducer;
import com.service.payment_system.repository.PaymentRepository;
import com.service.payment_system.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentProducer paymentProducer;
    private final PaymentRepository paymentRepository;
    private final StringRedisTemplate redisTemplate;

    @PostMapping
    public String transfer(@RequestBody PaymentRequest paymentRequest){

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(
                        paymentRequest.getIdempotencyKey(),
                        "PROCESSING",
                        Duration.ofMinutes(10)
                );

        if (Boolean.FALSE.equals(success)) {
            return "Duplicate Request";
        }

        Payment payment = Payment.builder()
                .idempotencyKey(paymentRequest.getIdempotencyKey())
                .senderId(paymentRequest.getSenderId())
                .receiverId(paymentRequest.getReceiverId())
                .amount(paymentRequest.getAmount())
                .status("PROCESSING")
                .createdAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        PaymentEvent paymentEvent = PaymentEvent.builder()
                .idempotencyKey(paymentRequest.getIdempotencyKey())
                .senderId(paymentRequest.getSenderId())
                .receiverId(paymentRequest.getReceiverId())
                .amount(paymentRequest.getAmount())
                .build();
        paymentProducer.sendPayment(paymentEvent);
        return "Payment Initiated";
    }
}
