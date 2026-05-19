package com.service.payment_system.service;

import com.service.payment_system.entity.Account;
import com.service.payment_system.entity.LedgerEntry;
import com.service.payment_system.entity.Payment;
import com.service.payment_system.event.PaymentEvent;
import com.service.payment_system.repository.AccountRepository;
import com.service.payment_system.repository.LedgerRepository;
import com.service.payment_system.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final AccountRepository accountRepository;
    private final PaymentRepository paymentRepository;
    private final StringRedisTemplate redisTemplate;
    private final LedgerRepository ledgerRepository;

    @Transactional
    public void processPayment(PaymentEvent event) {

        Account sender = accountRepository
                .findByIdForUpdate(event.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Account receiver = accountRepository
                .findByIdForUpdate(event.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Payment payment = paymentRepository
                .findByIdempotencyKey(event.getIdempotencyKey())
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        try {

            validateBalance(sender, event);

            debitSender(sender, event);

            // SIMULATE FAILURE
            // Uncomment for testing saga compensation

//            if (true) {
//                throw new RuntimeException("Receiver service failed");
//            }

            creditReceiver(receiver, event);

            markPaymentSuccess(payment, event);

            System.out.println("Payment Processed Successfully");

        } catch (Exception e) {

            compensateTransaction(sender, payment, event);

            System.out.println("Saga Compensation Executed");
        }
    }

    private void validateBalance(Account sender, PaymentEvent event) {

        if (sender.getBalance().compareTo(event.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
    }

    private void debitSender(Account sender, PaymentEvent event) {

        sender.setBalance(
                sender.getBalance().subtract(event.getAmount())
        );

        accountRepository.save(sender);

        ledgerRepository.save(
                LedgerEntry.builder()
                        .transactionId(event.getIdempotencyKey())
                        .accountId(sender.getId())
                        .entryType("DEBIT")
                        .amount(event.getAmount())
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    private void creditReceiver(Account receiver, PaymentEvent event) {

        receiver.setBalance(
                receiver.getBalance().add(event.getAmount())
        );

        accountRepository.save(receiver);

        ledgerRepository.save(
                LedgerEntry.builder()
                        .transactionId(event.getIdempotencyKey())
                        .accountId(receiver.getId())
                        .entryType("CREDIT")
                        .amount(event.getAmount())
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    private void markPaymentSuccess(Payment payment,
                                    PaymentEvent event) {

        payment.setStatus("SUCCESS");

        paymentRepository.save(payment);

        redisTemplate.opsForValue().set(
                event.getIdempotencyKey(),
                "SUCCESS",
                Duration.ofHours(1)
        );
    }

    private void compensateTransaction(Account sender,
                                       Payment payment,
                                       PaymentEvent event) {

        sender.setBalance(
                sender.getBalance().add(event.getAmount())
        );

        accountRepository.save(sender);

        ledgerRepository.save(
                LedgerEntry.builder()
                        .transactionId(event.getIdempotencyKey())
                        .accountId(sender.getId())
                        .entryType("REVERSAL")
                        .amount(event.getAmount())
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        payment.setStatus("REVERSED");

        paymentRepository.save(payment);
    }
}