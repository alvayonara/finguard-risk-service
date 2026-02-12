package com.alvayonara.finguardriskservice.risk.spending.summary;

import java.math.BigDecimal;
import java.util.List;

public record CurrentMonthSummary(
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    List<CategoryAmount> breakdown,
    String topCategory) {}
