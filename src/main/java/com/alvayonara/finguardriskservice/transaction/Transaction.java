package com.alvayonara.finguardriskservice.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("transactions")
public class Transaction {
  @Id private Long id;
  private Long userId;
  private TransactionType type;
  private BigDecimal amount;
  private Long categoryId;
  private LocalDate occurredAt;
  private LocalDateTime createdAt;
}
