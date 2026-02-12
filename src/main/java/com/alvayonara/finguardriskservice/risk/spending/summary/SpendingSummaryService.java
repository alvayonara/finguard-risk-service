package com.alvayonara.finguardriskservice.risk.spending.summary;

import com.alvayonara.finguardriskservice.transaction.TransactionRepository;
import com.alvayonara.finguardriskservice.transaction.TransactionType;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SpendingSummaryService {
  @Autowired private TransactionRepository transactionRepository;

  private static final BigDecimal ZERO = BigDecimal.ZERO;
  private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
  private static final BigDecimal TREND_THRESHOLD = new BigDecimal("5");

  public Mono<SpendingSummaryResponse> getSummary(Long userId, YearMonth month) {
    PeriodRange current = PeriodRange.of(month);
    PeriodRange previous = PeriodRange.of(month.minusMonths(1));
    Mono<List<TypeSumProjection>> typeSums =
        transactionRepository.sumByType(userId, current.start(), current.end()).collectList();
    Mono<List<CategorySumProjection>> categorySums =
        transactionRepository
            .sumExpenseByCategory(userId, current.start(), current.end())
            .collectList();
    Mono<BigDecimal> previousExpense =
        transactionRepository
            .sumExpenseOnly(userId, previous.start(), previous.end())
            .defaultIfEmpty(ZERO);

    return Mono.zip(typeSums, categorySums, previousExpense)
        .map(tuple -> buildResponse(month, tuple.getT1(), tuple.getT2(), tuple.getT3()));
  }

  private SpendingSummaryResponse buildResponse(
      YearMonth month,
      List<TypeSumProjection> typeSums,
      List<CategorySumProjection> categorySums,
      BigDecimal previousExpense) {
    BigDecimal totalIncome = ZERO;
    BigDecimal totalExpense = ZERO;

    for (TypeSumProjection projection : typeSums) {
      if (Objects.isNull(projection)) {
        continue;
      }

      BigDecimal total = Objects.requireNonNullElse(projection.total(), ZERO);
      if (TransactionType.INCOME.name().equals(projection.type())) {
        totalIncome = total;
      }
      if (TransactionType.EXPENSE.name().equals(projection.type())) {
        totalExpense = total;
      }
    }

    List<CategoryAmount> breakdown =
        categorySums.stream()
            .filter(Objects::nonNull)
            .map(
                categorySumProjection ->
                    new CategoryAmount(
                        categorySumProjection.category() == null
                            ? "UNCATEGORIZED"
                            : categorySumProjection.category(),
                        Objects.requireNonNullElse(categorySumProjection.total(), ZERO)))
            .sorted(
                Comparator.comparing(
                        CategoryAmount::getAmount, Comparator.nullsLast(BigDecimal::compareTo))
                    .reversed())
            .toList();

    String topCategory = breakdown.isEmpty() ? null : breakdown.get(0).getCategory();
    SpendingComparison comparison = buildComparison(totalExpense, previousExpense);
    return SpendingSummaryResponse.builder()
        .monthKey(month.toString())
        .totalIncome(totalIncome)
        .totalExpense(totalExpense)
        .topCategory(topCategory)
        .categoryBreakdown(breakdown)
        .comparison(comparison)
        .build();
  }

  private SpendingComparison buildComparison(BigDecimal current, BigDecimal previous) {
    if (Objects.isNull(previous) || previous.compareTo(ZERO) == 0) {
      return new SpendingComparison(ZERO, ZERO, "FLAT");
    }
    BigDecimal diff = current.subtract(previous);
    BigDecimal percentage =
        diff.divide(previous, MathContext.DECIMAL64)
            .multiply(ONE_HUNDRED)
            .setScale(2, BigDecimal.ROUND_HALF_UP);
    String trend;
    if (percentage.compareTo(TREND_THRESHOLD) > 0) {
      trend = "UP";
    } else if (percentage.compareTo(TREND_THRESHOLD.negate()) < 0) {
      trend = "DOWN";
    } else {
      trend = "FLAT";
    }
    return new SpendingComparison(previous, percentage, trend);
  }
}
