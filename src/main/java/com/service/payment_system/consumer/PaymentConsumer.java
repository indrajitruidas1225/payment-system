package com.service.payment_system.consumer;

import com.service.payment_system.event.PaymentEvent;
import com.service.payment_system.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentConsumer {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = "payment-event",
            groupId = "payment-group"
    )
    public void consume(PaymentEvent event) {

        paymentService.processPayment(event);
    }
}
