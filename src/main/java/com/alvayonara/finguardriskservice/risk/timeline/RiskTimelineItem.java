package com.alvayonara.finguardriskservice.risk.timeline;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskTimelineItem {
  private String level;
  private String signal;
  private LocalDateTime occurredAt;
}
