package com.alvayonara.finguardriskservice.risk;

import com.alvayonara.finguardriskservice.risk.config.RiskRuleConfig;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RiskRuleConfigRepository extends ReactiveCrudRepository<RiskRuleConfig, Long> {
    Mono<RiskRuleConfig> findByRuleName(String ruleName);
}
