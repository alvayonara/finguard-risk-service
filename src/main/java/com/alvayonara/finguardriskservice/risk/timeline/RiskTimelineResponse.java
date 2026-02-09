package com.alvayonara.finguardriskservice.risk.timeline;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RiskTimelineResponse {
    private List<RiskTimelineItem> items;
    private String nextCursorTime;
    private Long nextCursorId;
}
