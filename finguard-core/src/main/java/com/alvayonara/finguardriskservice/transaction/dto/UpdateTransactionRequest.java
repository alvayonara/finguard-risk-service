package com.alvayonara.finguardriskservice.transaction.dto;

import com.alvayonara.finguardriskservice.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class UpdateTransactionRequest {
  private TransactionType type;
  private BigDecimal amount;
  private Long categoryId;
  private LocalDate occurredAt;
}
