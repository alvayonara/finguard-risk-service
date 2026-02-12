package com.alvayonara.finguardriskservice.summary.spending;

import java.math.BigDecimal;

public record CategorySumProjection(String category, BigDecimal total) {}
