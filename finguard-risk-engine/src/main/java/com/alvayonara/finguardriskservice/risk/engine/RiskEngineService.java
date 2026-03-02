package com.alvayonara.finguardriskservice.risk.engine;

import com.alvayonara.finguardriskservice.risk.feature.config.RiskFeature;
import com.alvayonara.finguardriskservice.risk.level.RiskLevelConstants;
import com.alvayonara.finguardriskservice.risk.level.history.RiskLevelHistoryWriter;
import com.alvayonara.finguardriskservice.risk.rule.config.RiskRule;
import com.alvayonara.finguardriskservice.risk.signal.RiskSignal;
import com.alvayonara.finguardriskservice.risk.signal.RiskSignalRepository;
import com.alvayonara.finguardriskservice.risk.state.RiskChangeService;
import com.alvayonara.finguardriskservice.transaction.TransactionType;
import com.alvayonara.finguardriskservice.transaction.event.TransactionEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RiskEngineService {

  private final List<RiskFeature> features;
  private final List<RiskRule> rules;
  private final RiskSignalRepository riskSignalRepository;
  private final RiskChangeService riskChangeService;
  private final RiskLevelHistoryWriter riskLevelHistoryWriter;

  public RiskEngineService(
      List<RiskFeature> features,
      List<RiskRule> rules,
      RiskSignalRepository riskSignalRepository,
      RiskChangeService riskChangeService,
      RiskLevelHistoryWriter riskLevelHistoryWriter) {
    this.features = features;
    this.rules = rules;
    this.riskSignalRepository = riskSignalRepository;
    this.riskChangeService = riskChangeService;
    this.riskLevelHistoryWriter = riskLevelHistoryWriter;
  }

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
    return loadFeatures(context)
        .then(runRules(context))
        .then(upsertSignals(context))
        .then(updateState(context));
  }

  private RiskContext buildContext(TransactionEvent event) {
    RiskContext context = new RiskContext();
    context.setUserId(event.getUserId());
    context.setMonthKey(event.getOccurredAt().substring(0, 7));
    if (TransactionType.EXPENSE.equals(event.getType())) {
      context.getFeatures().put(RiskContextKeys.LATEST_EXPENSE, BigDecimal.valueOf(event.getAmount()));
      context.getFeatures().put(RiskContextKeys.LATEST_CATEGORY_ID, event.getCategoryId());
    }
    return context;
  }

  private Mono<Void> loadFeatures(RiskContext context) {
    return Flux.fromIterable(features).flatMap(feature -> feature.compute(context)).then();
  }

  private Mono<Void> runRules(RiskContext context) {
    return Flux.fromIterable(rules).flatMap(rule -> rule.evaluate(context)).then();
  }

  private Mono<Void> upsertSignals(RiskContext context) {
    return riskSignalRepository
        .deactivateSignalsForMonth(context.getUserId(), context.getMonthKey())
        .then(
            Flux.fromIterable(context.getSignals())
                .flatMap(
                    newSignal -> {
                      newSignal.setMonthKey(context.getMonthKey());
                      newSignal.setIsActive(true);
                      newSignal.setUpdatedAt(LocalDateTime.now());
                      return riskSignalRepository
                          .findByUserIdAndMonthKeyAndSignalType(
                              context.getUserId(), context.getMonthKey(), newSignal.getSignalType())
                          .flatMap(
                              existing -> {
                                existing.setSeverity(newSignal.getSeverity());
                                existing.setMetadata(newSignal.getMetadata());
                                existing.setIsActive(true);
                                existing.setUpdatedAt(LocalDateTime.now());
                                return riskSignalRepository.save(existing);
                              })
                          .switchIfEmpty(
                              Mono.defer(
                                  () -> {
                                    newSignal.setDetectedAt(LocalDateTime.now());
                                    return riskSignalRepository.save(newSignal);
                                  }));
                    })
                .then());
  }

  private Mono<Void> updateState(RiskContext context) {
    return Mono.defer(
        () -> {
          String latestLevel =
              context.getSignals().stream()
                  .map(RiskSignal::getSeverity)
                  .max(Comparator.comparingInt(this::severityWeight))
                  .orElse(RiskLevelConstants.LOW);
          String topSignal =
              context.getSignals().isEmpty()
                  ? RiskLevelConstants.NONE
                  : context.getSignals().getFirst().getSignalType();
          return riskChangeService
              .checkAndUpdate(context.getUserId(), latestLevel, topSignal)
              .flatMap(riskLevelHistoryWriter::insert)
              .then();
        });
  }

  private int severityWeight(String severity) {
    return switch (severity) {
      case RiskLevelConstants.HIGH -> RiskLevelConstants.SEVERITY_WEIGHT_HIGH;
      case RiskLevelConstants.MEDIUM -> RiskLevelConstants.SEVERITY_WEIGHT_MEDIUM;
      default -> RiskLevelConstants.SEVERITY_WEIGHT_LOW;
    };
  }
}
