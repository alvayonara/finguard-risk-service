package com.alvayonara.finguardriskservice.spending.summary;

import java.math.BigDecimal;
import java.util.List;

public record CurrentMonthSummary(
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    List<CategoryAmount> breakdown,
    String topCategory) {}
