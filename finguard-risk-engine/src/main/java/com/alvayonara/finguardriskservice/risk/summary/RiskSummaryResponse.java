package com.alvayonara.finguardriskservice.risk.summary;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskSummaryResponse {
  private String level;
  private int score;
  private String color;
  private String topInsightKey;
  private String recommendationKey;
  private String topSignalType;
  private int signalsCount;
  private LocalDateTime lastDetectedAt;
}
