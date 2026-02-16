package com.alvayonara.finguardriskservice.risk.metadata;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskSignalMetadata {
  private RuleInfo rule;
  private Map<String, Object> inputs;
  private Map<String, Object> computed;
  private Map<String, Object> context;

  @Data
  @Builder
  public static class RuleInfo {
    private String name;
    private Integer version;
  }
}
