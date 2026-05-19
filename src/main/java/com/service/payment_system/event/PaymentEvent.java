package com.service.payment_system.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentEvent {

    private String idempotencyKey;
    private Long senderId;
    private Long receiverId;
    private BigDecimal amount;

}
