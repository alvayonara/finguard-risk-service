package com.alvayonara.finguardriskservice.risk.trend;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskTrendResponse {
  private List<RiskTrendPoint> points;
}
