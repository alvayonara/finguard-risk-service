package com.alvayonara.finguardriskservice.risk.state;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("risk_state")
@Data
public class RiskState {
  @Id private Long userId;
  private String lastLevel;
  private LocalDateTime updatedAt;
}
