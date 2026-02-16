package com.alvayonara.finguardriskservice.risk.engine;

import com.alvayonara.finguardriskservice.risk.signal.RiskSignal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Data;

@Data
public class RiskContext {
  private Long userId;
  private String monthKey;
  private Map<String, Object> features = new ConcurrentHashMap<>();
  private List<RiskSignal> signals = new CopyOnWriteArrayList<>();
}
