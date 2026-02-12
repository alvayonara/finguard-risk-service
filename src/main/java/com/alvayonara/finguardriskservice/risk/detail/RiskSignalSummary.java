package com.alvayonara.finguardriskservice.risk.detail;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RiskSignalSummary {
  private String signalType;
  private String severity;
  private long occurrences;
  private LocalDateTime lastDetectedAt;
}
