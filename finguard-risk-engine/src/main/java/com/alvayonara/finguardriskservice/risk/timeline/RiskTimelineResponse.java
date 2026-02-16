package com.alvayonara.finguardriskservice.risk.timeline;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskTimelineResponse {
  private List<RiskTimelineItem> items;
  private String nextCursorTime;
  private Long nextCursorId;
}
