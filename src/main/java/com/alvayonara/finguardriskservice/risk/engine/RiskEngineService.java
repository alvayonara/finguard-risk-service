package com.alvayonara.finguardriskservice.risk.engine;

import com.alvayonara.finguardriskservice.risk.feature.config.RiskFeature;
import com.alvayonara.finguardriskservice.risk.level.history.RiskLevelHistoryWriter;
import com.alvayonara.finguardriskservice.risk.rule.config.RiskRule;
import com.alvayonara.finguardriskservice.risk.signal.RiskSignal;
import com.alvayonara.finguardriskservice.risk.signal.RiskSignalRepository;
import com.alvayonara.finguardriskservice.risk.state.RiskChangeService;
import com.alvayonara.finguardriskservice.transaction.TransactionType;
import com.alvayonara.finguardriskservice.transaction.event.TransactionEvent;
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

  public Mono<Void> handleCreated(TransactionEvent event) {
    return evaluateInternal(event);
  }

  public Mono<Void> handleUpdated(TransactionEvent event) {
    return evaluateInternal(event);
  }

  public Mono<Void> handleDeleted(TransactionEvent event) {
    return riskChangeService.recalculateUser(event.getUserId());
  }

  private Mono<Void> evaluateInternal(TransactionEvent event) {
    RiskContext context = buildContext(event);
    return clearSignalsForMonth(context)
        .then(loadFeatures(context))
        .then(runRules(context))
        .then(persistSignals(context))
        .then(updateState(context));
  }

  private RiskContext buildContext(TransactionEvent event) {
    RiskContext context = new RiskContext();
    context.setUserId(event.getUserId());
    context.setMonthKey(event.getOccurredAt().substring(0, 7));

    if (TransactionType.EXPENSE.equals(event.getType())) {
      context.getFeatures().put("latest_expense", BigDecimal.valueOf(event.getAmount()));
      context.getFeatures().put("latest_category_id", event.getCategoryId());
    }
    return context;
  }

  private Mono<Void> clearSignalsForMonth(RiskContext context) {
    return riskSignalRepository.deleteByUserIdAndMonthKey(
        context.getUserId(), context.getMonthKey());
  }

  private Mono<Void> loadFeatures(RiskContext context) {
    return Flux.fromIterable(features).flatMap(feature -> feature.compute(context)).then();
  }

  private Mono<Void> runRules(RiskContext context) {
    return Flux.fromIterable(rules).flatMap(rule -> rule.evaluate(context)).then();
  }

  private Mono<Void> persistSignals(RiskContext context) {
    return Flux.fromIterable(context.getSignals())
        .doOnNext(signal -> signal.setMonthKey(context.getMonthKey()))
        .flatMap(riskSignalRepository::save)
        .then();
  }

  private Mono<Void> updateState(RiskContext context) {
    return Mono.defer(
        () -> {
          String latestLevel =
              context.getSignals().stream()
                  .map(RiskSignal::getSeverity)
                  .max(Comparator.comparingInt(this::severityWeight))
                  .orElse("LOW");
          String topSignal =
              context.getSignals().isEmpty() ? "NONE" : context.getSignals().get(0).getSignalType();
          return riskChangeService
              .checkAndUpdate(context.getUserId(), latestLevel, topSignal)
              .flatMap(riskLevelHistoryWriter::insert)
              .then();
        });
  }

  private int severityWeight(String severity) {
    return switch (severity) {
      case "HIGH" -> 3;
      case "MEDIUM" -> 2;
      default -> 1;
    };
  }
}
