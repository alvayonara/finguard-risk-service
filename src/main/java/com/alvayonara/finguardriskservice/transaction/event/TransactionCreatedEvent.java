package com.alvayonara.finguardriskservice.transaction.event;

import com.alvayonara.finguardriskservice.transaction.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCreatedEvent {
  private Long transactionId;
  private Long userId;
  private TransactionType type;
  private Long categoryId;
  private Double amount;
  private String occurredAt;
}
