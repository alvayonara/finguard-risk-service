package com.alvayonara.finguardriskservice.transaction.dto;

import com.alvayonara.finguardriskservice.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RecentTransactionProjection(
    TransactionType type, BigDecimal amount, String category, LocalDate occurredAt) {}
