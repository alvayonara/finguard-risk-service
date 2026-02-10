package com.alvayonara.finguardriskservice.risk.engine;

import com.alvayonara.finguardriskservice.risk.feature.RiskFeature;
import com.alvayonara.finguardriskservice.risk.level.history.RiskLevelHistoryWriter;
import com.alvayonara.finguardriskservice.risk.rule.RiskRule;
import com.alvayonara.finguardriskservice.risk.signal.RiskSignal;
import com.alvayonara.finguardriskservice.risk.signal.RiskSignalRepository;
import com.alvayonara.finguardriskservice.risk.state.RiskChangeService;
import com.alvayonara.finguardriskservice.transaction.event.TransactionCreatedEvent;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RiskEngineService {
  @Autowired private List<RiskFeature> features;
  @Autowired private List<RiskRule> rules;
  @Autowired private RiskSignalRepository riskSignalRepository;
  @Autowired private RiskChangeService riskChangeService;
  @Autowired private RiskLevelHistoryWriter riskLevelHistoryWriter;

  public Mono<Void> evaluate(TransactionCreatedEvent event) {
    RiskContext context = new RiskContext();
    context.setUserId(event.getUserId());
    String monthKey = event.getOccurredAt().substring(0, 7);
    context.setMonthKey(monthKey);
    if ("EXPENSE".equals(event.getType())) {
      context.getFeatures().put("latest_expense", BigDecimal.valueOf(event.getAmount()));
    }

    Mono<Void> loadFeatures =
        Flux.fromIterable(features).flatMap(feature -> feature.compute(context)).then();
    Mono<Void> runRules = Flux.fromIterable(rules).flatMap(rule -> rule.evaluate(context)).then();
    Mono<Void> persistSignals =
        Flux.fromIterable(context.getSignals()).flatMap(riskSignalRepository::save).then();
    Mono<Void> updateState =
        Mono.defer(
            () -> {
              String latestLevel =
                  context.getSignals().stream()
                      .map(RiskSignal::getSeverity)
                      .max(Comparator.comparingInt(this::severityWeight))
                      .orElse("LOW");
              String topSignal =
                  context.getSignals().isEmpty()
                      ? "NONE"
                      : context.getSignals().get(0).getSignalType();
              return riskChangeService
                  .checkAndUpdate(event.getUserId(), latestLevel, topSignal)
                  .flatMap(evt -> riskLevelHistoryWriter.insert(evt))
                  .then();
            });

    return loadFeatures.then(runRules).then(persistSignals).then(updateState);
  }

  private int severityWeight(String s) {
    return switch (s) {
      case "HIGH" -> 3;
      case "MEDIUM" -> 2;
      default -> 1;
    };
  }
}
