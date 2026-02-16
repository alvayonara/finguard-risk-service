package com.alvayonara.finguardriskservice.activity.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionActivityData(
    Long id,
    String type,
    BigDecimal amount,
    CategoryInfo category,
    LocalDate occurredAt,
    LocalDateTime createdAt) {
  public record CategoryInfo(Long id, String name, String icon, String color) {}
}
