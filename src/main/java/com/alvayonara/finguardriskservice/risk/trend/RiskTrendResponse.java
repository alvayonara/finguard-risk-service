package com.alvayonara.finguardriskservice.risk.trend;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RiskTrendResponse {
    private List<RiskTrendPoint> points;
}
