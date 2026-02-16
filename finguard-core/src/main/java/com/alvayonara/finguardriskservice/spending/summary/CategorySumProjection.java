package com.alvayonara.finguardriskservice.spending.summary;

import java.math.BigDecimal;

public record CategorySumProjection(Long categoryId, BigDecimal total) {}
