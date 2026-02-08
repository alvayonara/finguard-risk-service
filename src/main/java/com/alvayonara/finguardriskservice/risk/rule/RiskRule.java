package com.alvayonara.finguardriskservice.risk.rule;

import com.alvayonara.finguardriskservice.risk.engine.RiskContext;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RiskRule {
    String name();
    List<String> requiredFeatures();
    Mono<Void> evaluate(RiskContext context);
}
