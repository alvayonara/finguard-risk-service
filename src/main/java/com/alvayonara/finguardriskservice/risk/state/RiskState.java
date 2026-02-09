package com.alvayonara.finguardriskservice.risk.state;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("risk_state")
@Data
public class RiskState {
    @Id
    private Long userId;
    private String lastLevel;
    private LocalDateTime updatedAt;
}
