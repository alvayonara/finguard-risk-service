package com.alvayonara.finguardriskservice.risk.rule.config;

import com.alvayonara.finguardriskservice.risk.config.RiskRuleConfig;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RiskRuleConfigService {
  @Autowired private RiskRuleConfigRepository riskRuleConfigRepository;
  private final Map<String, RiskRuleConfig> cache = new ConcurrentHashMap<>();

  public Mono<RiskRuleConfig> get(String ruleName) {
    RiskRuleConfig cached = cache.get(ruleName);
    if (Objects.nonNull(cached)) {
      return Mono.just(cached);
    }
    return riskRuleConfigRepository
        .findByRuleName(ruleName)
        .doOnNext(config -> cache.put(ruleName, config));
  }
}
