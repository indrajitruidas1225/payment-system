package com.service.payment_system.producer;

import com.service.payment_system.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public void sendPayment(PaymentEvent event){
        kafkaTemplate.send("payment-event", event);
    }
}
