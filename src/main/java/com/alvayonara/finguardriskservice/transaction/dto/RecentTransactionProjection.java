package com.alvayonara.finguardriskservice.transaction.dto;

import com.alvayonara.finguardriskservice.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RecentTransactionProjection(
    Long id,
    TransactionType type,
    BigDecimal amount,
    Long categoryId,
    String category,
    LocalDate occurredAt) {}
