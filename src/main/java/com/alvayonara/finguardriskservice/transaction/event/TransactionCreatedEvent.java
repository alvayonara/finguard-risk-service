package com.alvayonara.finguardriskservice.transaction.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCreatedEvent {
  private Long transactionId;
  private Long userId;
  private String type;
  private Double amount;
  private String occurredAt;
}
