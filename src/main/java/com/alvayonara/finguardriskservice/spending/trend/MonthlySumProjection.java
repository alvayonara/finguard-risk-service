package com.alvayonara.finguardriskservice.spending.trend;

import java.math.BigDecimal;

public record MonthlySumProjection(Integer year, Integer month, BigDecimal total) {}
