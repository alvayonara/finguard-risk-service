package com.alvayonara.finguardriskservice.summary;

import com.alvayonara.finguardriskservice.transaction.event.TransactionCreatedEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MonthlySummaryService {
  @Autowired private MonthlySummaryRepository monthlySummaryRepository;

  public Mono<Void> applyTransaction(TransactionCreatedEvent event) {
    String monthKey = event.getOccurredAt().substring(0, 7);
    return monthlySummaryRepository
        .findByUserIdAndMonthKey(event.getUserId(), monthKey)
        .flatMap(existing -> updateExisting(existing, event))
        .switchIfEmpty(insertNew(event.getUserId(), monthKey, event))
        .then();
  }

  private Mono<MonthlySummary> updateExisting(
      MonthlySummary summary, TransactionCreatedEvent event) {
    applyAmount(summary, event);
    summary.setUpdatedAt(LocalDateTime.now());
    return monthlySummaryRepository.save(summary);
  }

  private Mono<MonthlySummary> insertNew(
      Long userId, String monthKey, TransactionCreatedEvent event) {
    MonthlySummary summary = new MonthlySummary();
    summary.setUserId(userId);
    summary.setMonthKey(monthKey);
    summary.setTotalIncome(BigDecimal.ZERO);
    summary.setTotalExpense(BigDecimal.ZERO);
    applyAmount(summary, event);
    summary.setUpdatedAt(LocalDateTime.now());
    return monthlySummaryRepository.save(summary);
  }

  private void applyAmount(MonthlySummary summary, TransactionCreatedEvent event) {
    if ("INCOME".equals(event.getType().name())) {
      summary.setTotalIncome(summary.getTotalIncome().add(BigDecimal.valueOf(event.getAmount())));
    } else {
      summary.setTotalExpense(summary.getTotalExpense().add(BigDecimal.valueOf(event.getAmount())));
    }
  }
}
