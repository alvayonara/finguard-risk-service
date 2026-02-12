package com.alvayonara.finguardriskservice.risk.spending.trend;

import java.math.BigDecimal;

public record MonthlySumProjection(Integer year, Integer month, BigDecimal total) {}
