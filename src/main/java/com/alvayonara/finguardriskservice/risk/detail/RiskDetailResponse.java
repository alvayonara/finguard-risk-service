package com.alvayonara.finguardriskservice.risk.detail;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskDetailResponse {
  private String currentLevel;
  private Integer score;
  private String color;
  private String topInsightKey;
  private String recommendationKey;
  private LocalDateTime lastDetectedAt;
  private List<ActiveSignalItem> activeSignals;
  private List<LevelChangeItem> recentLevelChanges;
  private List<RiskSignalSummary> activeSignalSummaries;

  @Data
  @Builder
  public static class ActiveSignalItem {
    private String signalType;
    private String severity;
    private LocalDateTime detectedAt;
  }

  @Data
  @Builder
  public static class LevelChangeItem {
    private String oldLevel;
    private String newLevel;
    private LocalDateTime occurredAt;
  }
}
