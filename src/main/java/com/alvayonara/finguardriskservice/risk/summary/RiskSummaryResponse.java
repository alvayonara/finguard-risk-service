package com.alvayonara.finguardriskservice.risk.summary;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RiskSummaryResponse {
    private String level;
    private int score;
    private String color;
    private String topInsight;
    private String topSignalType;
    private int signalsCount;
    private LocalDateTime lastDetectedAt;
}
