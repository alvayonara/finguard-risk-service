package com.alvayonara.finguardriskservice.transaction.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCreatedEvent {
    private Long transactionId;
    private Long userId;
    private String type;
    private BigDecimal amount;
    private String occurredAt;
}
