package com.alvayonara.finguardriskservice.risk.rule;

import com.alvayonara.finguardriskservice.common.JsonUtil;
import com.alvayonara.finguardriskservice.risk.RiskRuleConfigService;
import com.alvayonara.finguardriskservice.risk.RiskSignal;
import com.alvayonara.finguardriskservice.risk.constants.FeatureConstants;
import com.alvayonara.finguardriskservice.risk.constants.RuleConstants;
import com.alvayonara.finguardriskservice.risk.engine.RiskContext;
import com.alvayonara.finguardriskservice.risk.metadata.RiskSignalMetadata;
import com.alvayonara.finguardriskservice.summary.MonthlySummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class NegativeCashFlowRule implements RiskRule {
    @Autowired
    private RiskRuleConfigService configService;
    @Autowired
    private JsonUtil jsonUtil;

    @Override
    public String name() {
        return RuleConstants.NEGATIVE_CASH_FLOW;
    }

    @Override
    public List<String> requiredFeatures() {
        return List.of(FeatureConstants.MONTHLY_SUMMARY);
    }

    @Override
    public Mono<Void> evaluate(RiskContext context) {
        return configService.get(name())
                .flatMap(config -> {
                    if (!config.getEnabled()) {
                        return Mono.empty();
                    }
                    MonthlySummary summary = (MonthlySummary) context.getFeatures().get(FeatureConstants.MONTHLY_SUMMARY);
                    if (Objects.isNull(summary)) {
                        return Mono.empty();
                    }
                    BigDecimal threshold = config.getThresholdValue();
                    boolean risky = summary.getTotalExpense().compareTo(summary.getTotalIncome().multiply(threshold)) > 0;
                    if (risky) {
                        RiskSignal signal = new RiskSignal();
                        signal.setUserId(context.getUserId());
                        signal.setSignalType(name());
                        signal.setSeverity(config.getSeverity());
                        signal.setDetectedAt(LocalDateTime.now());
                        buildMetadata(context, summary, threshold, signal);
                        context.getSignals().add(signal);
                    }
                    return Mono.empty();
                });
    }

    private void buildMetadata(RiskContext context, MonthlySummary summary, BigDecimal threshold, RiskSignal signal) {
        RiskSignalMetadata metadata = RiskSignalMetadata.builder()
                .rule(RiskSignalMetadata.RuleInfo.builder()
                        .name(name())
                        .version(1)
                        .build())
                .inputs(Map.of(
                        "income", summary.getTotalIncome(),
                        "expense", summary.getTotalExpense()
                ))
                .computed(Map.of(
                        "threshold", threshold
                ))
                .context(Map.of(
                        "monthKey", context.getMonthKey()
                ))
                .build();
        signal.setMetadata(jsonUtil.toJson(metadata));
    }
}
