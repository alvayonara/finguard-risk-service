package com.alvayonara.finguardriskservice.risk.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RiskLevelChangedEvent {
  private Long userId;
  private String oldLevel;
  private String newLevel;
  private String topSignalType;
  private LocalDateTime occurredAt;
}
