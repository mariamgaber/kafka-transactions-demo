package com.kafka.payments.core.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawalRequestedEvent {
    private String senderId;
    private String recipientId;
    private BigDecimal amount;

}