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
  private LocalDateTime detectedAt;
  private String metadata;
}
