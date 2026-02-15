package com.alvayonara.finguardriskservice.risk.signal;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("risk_signal")
public class RiskSignal {
  @Id private Long id;
  private Long userId;
  private String signalType;
  private String severity;
  private String monthKey;
  private LocalDateTime detectedAt;
  private LocalDateTime updatedAt;
  private String metadata;
  private Boolean isActive;
}
