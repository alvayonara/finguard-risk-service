package com.alvayonara.finguardriskservice.risk.timeline;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RiskTimelineItem {
    private String level;
    private String signal;
    private LocalDateTime occurredAt;
}
