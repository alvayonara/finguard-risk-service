package com.alvayonara.finguardriskservice.risk.level;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskLevelResponse {
    private String level;
    private int score;
    private String color;
}
