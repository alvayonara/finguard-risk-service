package com.alvayonara.finguardriskservice.risk.feature;

import com.alvayonara.finguardriskservice.risk.engine.RiskContext;
import reactor.core.publisher.Mono;

public interface RiskFeature {
    String name();
    Mono<Void> compute(RiskContext riskContext);
}
