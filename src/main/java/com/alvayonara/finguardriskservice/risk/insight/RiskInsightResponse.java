package com.alvayonara.finguardriskservice.risk.insight;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RiskInsightResponse {
    private String type;
    private String severity;
    private String message;
    private LocalDateTime detectedAt;
}
