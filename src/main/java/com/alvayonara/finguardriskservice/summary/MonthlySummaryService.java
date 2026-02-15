package com.alvayonara.finguardriskservice.summary;

import com.alvayonara.finguardriskservice.transaction.TransactionRepository;
import com.alvayonara.finguardriskservice.transaction.TransactionType;
import com.alvayonara.finguardriskservice.transaction.event.TransactionEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MonthlySummaryService {

  @Autowired private MonthlySummaryRepository monthlySummaryRepository;
  @Autowired private TransactionRepository transactionRepository;

  private static final BigDecimal ZERO = BigDecimal.ZERO;

  public Mono<Void> handleCreated(TransactionEvent event) {
    return applyIncremental(event);
  }

  public Mono<Void> handleUpdated(TransactionEvent event) {
    return recalculateMonth(event.getUserId(), event.getOccurredAt());
  }

  public Mono<Void> handleDeleted(TransactionEvent event) {
    return recalculateMonth(event.getUserId(), event.getOccurredAt());
  }

  private Mono<Void> applyIncremental(TransactionEvent event) {
    String monthKey = event.getOccurredAt().substring(0, 7);
    return monthlySummaryRepository
        .findByUserIdAndMonthKey(event.getUserId(), monthKey)
        .flatMap(existing -> updateExisting(existing, event))
        .switchIfEmpty(insertNew(event.getUserId(), monthKey, event))
        .then();
  }

  private Mono<MonthlySummary> updateExisting(MonthlySummary summary, TransactionEvent event) {
    applyAmount(summary, event);
    summary.setUpdatedAt(LocalDateTime.now());
    return monthlySummaryRepository.save(summary);
  }

  private Mono<MonthlySummary> insertNew(Long userId, String monthKey, TransactionEvent event) {
    MonthlySummary summary =
        MonthlySummary.builder()
            .userId(userId)
            .monthKey(monthKey)
            .totalIncome(ZERO)
            .totalExpense(ZERO)
            .build();
    applyAmount(summary, event);
    summary.setUpdatedAt(LocalDateTime.now());
    return monthlySummaryRepository.save(summary);
  }

  private void applyAmount(MonthlySummary summary, TransactionEvent event) {
    BigDecimal amount = BigDecimal.valueOf(event.getAmount());
    if (TransactionType.INCOME.equals(event.getType())) {
      summary.setTotalIncome(summary.getTotalIncome().add(amount));
    } else {
      summary.setTotalExpense(summary.getTotalExpense().add(amount));
    }
  }

  public Mono<Void> recalculateMonth(Long userId, String occurredAt) {
    YearMonth yearMonth = YearMonth.parse(occurredAt.substring(0, 7));
    LocalDate start = yearMonth.atDay(1);
    LocalDate end = yearMonth.plusMonths(1).atDay(1);

    return transactionRepository
        .sumByType(userId, start, end)
        .collectList()
        .flatMap(
            typeSums -> {
              BigDecimal income = ZERO;
              BigDecimal expense = ZERO;

              for (var sum : typeSums) {
                if (sum.total() != null) {
                  if (TransactionType.INCOME.name().equals(sum.type())) {
                    income = sum.total();
                  } else if (TransactionType.EXPENSE.name().equals(sum.type())) {
                    expense = sum.total();
                  }
                }
              }

              final BigDecimal finalIncome = income;
              final BigDecimal finalExpense = expense;

              return monthlySummaryRepository
                  .findByUserIdAndMonthKey(userId, yearMonth.toString())
                  .flatMap(
                      existing -> {
                        existing.setTotalIncome(finalIncome);
                        existing.setTotalExpense(finalExpense);
                        existing.setUpdatedAt(LocalDateTime.now());
                        return monthlySummaryRepository.save(existing);
                      })
                  .switchIfEmpty(
                      monthlySummaryRepository.save(
                          MonthlySummary.builder()
                              .userId(userId)
                              .monthKey(yearMonth.toString())
                              .totalIncome(finalIncome)
                              .totalExpense(finalExpense)
                              .updatedAt(LocalDateTime.now())
                              .build()));
            })
        .then();
  }
}
