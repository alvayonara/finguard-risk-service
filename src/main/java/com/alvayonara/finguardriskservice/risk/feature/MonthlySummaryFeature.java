package com.alvayonara.finguardriskservice.risk.feature;

import com.alvayonara.finguardriskservice.risk.engine.RiskContext;
import com.alvayonara.finguardriskservice.summary.MonthlySummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MonthlySummaryFeature implements RiskFeature {
    @Autowired
    private MonthlySummaryRepository monthlySummaryRepository;

    @Override
    public String name() {
        return FeatureConstants.MONTHLY_SUMMARY;
    }

    @Override
    public Mono<Void> compute(RiskContext context) {
        return monthlySummaryRepository
                .findByUserIdAndMonthKey(context.getUserId(), context.getMonthKey())
                .doOnNext(summary -> context.getFeatures().put(name(), summary))
                .then();
    }
}
