package com.kafka.transfers.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferRestModel {
    private String senderId;
    private String recipientId;
    private BigDecimal amount;
    
}
