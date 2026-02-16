package com.alvayonara.finguardriskservice.risk.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("risk_rule_config")
@Data
public class RiskRuleConfig {
  @Id private Long id;
  private String ruleName;
  private Boolean enabled;
  private String severity;
  private BigDecimal thresholdValue;
  private LocalDateTime updatedAt;
}
