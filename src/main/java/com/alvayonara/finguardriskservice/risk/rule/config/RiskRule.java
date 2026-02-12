package com.alvayonara.finguardriskservice.risk.rule.config;

import com.alvayonara.finguardriskservice.risk.engine.RiskContext;
import java.util.List;
import reactor.core.publisher.Mono;

public interface RiskRule {
  String name();

  List<String> requiredFeatures();

  Mono<Void> evaluate(RiskContext context);
}
