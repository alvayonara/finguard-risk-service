package com.alvayonara.finguardriskservice.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CreateTransactionRequest {
  private String type;
  private BigDecimal amount;
  private String category;
  private LocalDate occurredAt;
}
