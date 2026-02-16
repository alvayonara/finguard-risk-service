package com.alvayonara.finguardriskservice.budget;

import com.alvayonara.finguardriskservice.budget.dto.BudgetUsageResponse;
import com.alvayonara.finguardriskservice.category.CategoryRepository;
import com.alvayonara.finguardriskservice.spending.summary.CategorySumProjection;
import com.alvayonara.finguardriskservice.spending.summary.PeriodRange;
import com.alvayonara.finguardriskservice.transaction.TransactionRepository;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BudgetService {
  @Autowired private BudgetConfigRepository budgetConfigRepository;
  @Autowired private TransactionRepository transactionRepository;
  @Autowired private CategoryRepository categoryRepository;

  private static final BigDecimal ZERO = BigDecimal.ZERO;
  private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

  public Flux<BudgetUsageResponse> getMonthlyBudgetUsage(Long userId, YearMonth month) {
    PeriodRange range = PeriodRange.of(month);
    Mono<List<CategorySumProjection>> expenseSums =
        transactionRepository
            .sumExpenseByCategory(userId, range.start(), range.end())
            .collectList();
    Mono<List<BudgetConfig>> budgets = budgetConfigRepository.findByUserId(userId).collectList();
    return Mono.zip(expenseSums, budgets)
        .flatMapMany(
            tuple -> {
              Map<Long, BigDecimal> spentMap =
                  tuple.getT1().stream()
                      .collect(
                          Collectors.toMap(
                              CategorySumProjection::categoryId,
                              categorySumProjection ->
                                  categorySumProjection.total() == null
                                      ? ZERO
                                      : categorySumProjection.total()));
              return Flux.fromIterable(tuple.getT2())
                  .flatMap(
                      config ->
                          categoryRepository
                              .findById(config.getCategoryId())
                              .map(
                                  category -> {
                                    BigDecimal spent =
                                        spentMap.getOrDefault(config.getCategoryId(), ZERO);
                                    BigDecimal remaining = config.getMonthlyLimit().subtract(spent);
                                    BigDecimal percentage =
                                        config.getMonthlyLimit().compareTo(ZERO) == 0
                                            ? ZERO
                                            : spent
                                                .divide(
                                                    config.getMonthlyLimit(), MathContext.DECIMAL64)
                                                .multiply(ONE_HUNDRED)
                                                .setScale(2, BigDecimal.ROUND_HALF_UP);
                                    return BudgetUsageResponse.builder()
                                        .category(category.getName())
                                        .monthlyLimit(config.getMonthlyLimit())
                                        .spent(spent)
                                        .remaining(remaining)
                                        .percentageUsed(percentage)
                                        .build();
                                  }));
            });
  }

  public Mono<BudgetConfig> upsertBudget(Long userId, Long categoryId, BigDecimal monthlyLimit) {
    return budgetConfigRepository
        .findByUserIdAndCategoryId(userId, categoryId)
        .flatMap(
            existing -> {
              existing.setMonthlyLimit(monthlyLimit);
              return budgetConfigRepository.save(existing);
            })
        .switchIfEmpty(
            budgetConfigRepository.save(
                BudgetConfig.builder()
                    .userId(userId)
                    .categoryId(categoryId)
                    .monthlyLimit(monthlyLimit)
                    .build()));
  }
}
