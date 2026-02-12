package com.alvayonara.finguardriskservice.risk.spending.trend;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SpendingTrendResponse {
  List<MonthlyPoint> points;

  @Value
  @Builder
  public static class MonthlyPoint {
    String monthKey;
    BigDecimal expense;
  }
}
