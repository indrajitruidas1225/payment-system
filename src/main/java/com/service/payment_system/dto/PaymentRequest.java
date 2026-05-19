package com.service.payment_system.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private String idempotencyKey;
    private Long senderId;
    private Long receiverId;
    private BigDecimal amount;
}
