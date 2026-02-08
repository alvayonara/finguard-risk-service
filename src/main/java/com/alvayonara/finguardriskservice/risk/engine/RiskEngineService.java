package com.alvayonara.finguardriskservice.risk.engine;

import com.alvayonara.finguardriskservice.risk.signal.RiskSignalRepository;
import com.alvayonara.finguardriskservice.risk.feature.RiskFeature;
import com.alvayonara.finguardriskservice.risk.rule.RiskRule;
import com.alvayonara.finguardriskservice.transaction.event.TransactionCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RiskEngineService {
    @Autowired
    private List<RiskFeature> features;
    @Autowired
    private List<RiskRule> rules;
    @Autowired
    private RiskSignalRepository riskSignalRepository;

    public Mono<Void> evaluate(TransactionCreatedEvent event) {
        RiskContext context = new RiskContext();
        context.setUserId(event.getUserId());
        String monthKey = event.getOccurredAt().substring(0, 7);
        context.setMonthKey(monthKey);
        if ("EXPENSE".equals(event.getType())) {
            context.getFeatures().put("latest_expense", BigDecimal.valueOf(event.getAmount()));
        }

        Mono<Void> loadFeatures = Flux.fromIterable(features)
                        .flatMap(feature -> feature.compute(context))
                        .then();
        Mono<Void> runRules = Flux.fromIterable(rules)
                        .flatMap(rule -> rule.evaluate(context))
                        .then();
        Mono<Void> persistSignals = Flux.fromIterable(context.getSignals())
                        .flatMap(riskSignalRepository::save)
                        .then();
        return loadFeatures.then(runRules).then(persistSignals);
    }
}
