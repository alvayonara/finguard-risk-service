package com.alvayonara.finguardriskservice.risk.trend;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskTrendPoint {
  private String date;
  private String level;
  private int score;
}
