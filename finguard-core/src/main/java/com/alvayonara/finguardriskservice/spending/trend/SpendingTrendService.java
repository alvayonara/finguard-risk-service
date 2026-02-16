package com.alvayonara.finguardriskservice.spending.trend;

import com.alvayonara.finguardriskservice.transaction.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SpendingTrendService {
  @Autowired private TransactionRepository transactionRepository;
  private static final BigDecimal ZERO = BigDecimal.ZERO;

  public Mono<SpendingTrendResponse> getTrend(Long userId, int months) {
    YearMonth currentMonth = YearMonth.now();
    YearMonth startMonth = currentMonth.minusMonths(months - 1);
    LocalDate startDate = startMonth.atDay(1);
    LocalDate endDate = currentMonth.plusMonths(1).atDay(1);
    return transactionRepository
        .sumExpenseGroupedByMonth(userId, startDate, endDate)
        .collectList()
        .map(projections -> buildTrendResponse(startMonth, currentMonth, projections));
  }

  private SpendingTrendResponse buildTrendResponse(
      YearMonth startMonth, YearMonth endMonth, List<MonthlySumProjection> projections) {
    Map<YearMonth, BigDecimal> monthSumMap =
        projections.stream()
            .collect(
                Collectors.toMap(
                    monthlySumProjection ->
                        YearMonth.of(monthlySumProjection.year(), monthlySumProjection.month()),
                    monthlySumProjection ->
                        Optional.ofNullable(monthlySumProjection.total()).orElse(ZERO)));

    List<SpendingTrendResponse.MonthlyPoint> points = new ArrayList<>();
    YearMonth pointer = startMonth;
    while (!pointer.isAfter(endMonth)) {
      BigDecimal value = monthSumMap.getOrDefault(pointer, ZERO);
      points.add(
          SpendingTrendResponse.MonthlyPoint.builder()
              .monthKey(pointer.toString())
              .expense(value)
              .build());
      pointer = pointer.plusMonths(1);
    }
    return SpendingTrendResponse.builder().points(points).build();
  }
}
