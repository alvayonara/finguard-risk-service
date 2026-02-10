package com.alvayonara.finguardriskservice.risk.feature;

import com.alvayonara.finguardriskservice.risk.engine.RiskContext;
import com.alvayonara.finguardriskservice.transaction.TransactionRepository;
import java.math.BigDecimal;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AvgExpense30dFeature implements RiskFeature {
  @Autowired private TransactionRepository transactionRepository;

  @Override
  public String name() {
    return FeatureConstants.AVG_EXPENSE_30D;
  }

  @Override
  public Mono<Void> compute(RiskContext context) {
    return transactionRepository
        .findAvgExpenseLast30Days(context.getUserId())
        .doOnNext(
            avg -> context.getFeatures().put(name(), Objects.isNull(avg) ? BigDecimal.ZERO : avg))
        .then();
  }
}
