package com.alvayonara.finguardriskservice.risk.level.history;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("risk_level_history")
@Data
public class RiskLevelHistory {
    @Id
    private Long id;
    private Long userId;
    private String oldLevel;
    private String newLevel;
    private String topSignalType;
    private LocalDateTime occurredAt;
}
