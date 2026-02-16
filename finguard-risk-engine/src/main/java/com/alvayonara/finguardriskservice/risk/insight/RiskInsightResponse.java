package com.alvayonara.finguardriskservice.risk.insight;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskInsightResponse {
  private String type;
  private String severity;
  private String message;
  private LocalDateTime detectedAt;
}
