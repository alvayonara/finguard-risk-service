package com.alvayonara.finguardriskservice.budget;

import com.alvayonara.finguardriskservice.budget.dto.BudgetUsagePageResponse;
import com.alvayonara.finguardriskservice.budget.dto.BudgetUsageResponse;
import com.alvayonara.finguardriskservice.category.CategoryRepository;
import com.alvayonara.finguardriskservice.spending.summary.CategorySumProjection;
import com.alvayonara.finguardriskservice.spending.summary.PeriodRange;
import com.alvayonara.finguardriskservice.transaction.TransactionRepository;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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
    @Autowired
    private BudgetConfigRepository budgetConfigRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

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

    public Mono<Void> deleteBudget(Long userId, Long categoryId) {
        return budgetConfigRepository
                .findByUserIdAndCategoryId(userId, categoryId)
                .flatMap(budgetConfigRepository::delete);
    }

    public Mono<BudgetUsagePageResponse> getMonthlyBudgetUsagePaginated(
            Long userId, YearMonth month, String cursorTime, Long cursorId, int limit) {
        PeriodRange range = PeriodRange.of(month);
        Flux<BudgetConfig> budgetFlux =
                cursorTime == null || cursorId == null
                        ? budgetConfigRepository.findFirstPageByUserId(userId, limit + 1)
                        : budgetConfigRepository.findNextPageByUserId(
                        userId, LocalDateTime.parse(cursorTime), cursorId, limit + 1);

        Mono<List<CategorySumProjection>> expenseSums =
                transactionRepository
                        .sumExpenseByCategory(userId, range.start(), range.end())
                        .collectList();

        return Mono.zip(budgetFlux.collectList(), expenseSums)
                .flatMap(
                        tuple -> {
                            List<BudgetConfig> configs = tuple.getT1();
                            Map<Long, BigDecimal> spentMap =
                                    tuple.getT2().stream()
                                            .collect(
                                                    Collectors.toMap(
                                                            CategorySumProjection::categoryId,
                                                            categorySumProjection ->
                                                                    categorySumProjection.total() == null
                                                                            ? ZERO
                                                                            : categorySumProjection.total()));

                            boolean hasMore = configs.size() > limit;
                            List<BudgetConfig> pageConfigs =
                                    hasMore ? configs.subList(0, limit) : configs;

                            String nextCursorTime = null;
                            Long nextCursorId = null;
                            if (hasMore && !pageConfigs.isEmpty()) {
                                BudgetConfig last = pageConfigs.get(pageConfigs.size() - 1);
                                nextCursorTime = last.getCreatedAt().toString();
                                nextCursorId = last.getId();
                            }

                            Flux<BudgetUsageResponse> responseFlux =
                                    Flux.fromIterable(pageConfigs)
                                            .flatMap(
                                                    config ->
                                                            categoryRepository
                                                                    .findById(config.getCategoryId())
                                                                    .map(
                                                                            category -> {
                                                                                BigDecimal spent =
                                                                                        spentMap.getOrDefault(
                                                                                                config.getCategoryId(), ZERO);
                                                                                BigDecimal remaining =
                                                                                        config.getMonthlyLimit().subtract(spent);
                                                                                BigDecimal percentage =
                                                                                        config.getMonthlyLimit().compareTo(ZERO) == 0
                                                                                                ? ZERO
                                                                                                : spent.divide(
                                                                                                config.getMonthlyLimit(),
                                                                                                MathContext.DECIMAL64)
                                                                                                .multiply(ONE_HUNDRED)
                                                                                                .setScale(
                                                                                                        2, RoundingMode.HALF_UP);
                                                                                return BudgetUsageResponse.builder()
                                                                                        .category(category.getName())
                                                                                        .monthlyLimit(config.getMonthlyLimit())
                                                                                        .spent(spent)
                                                                                        .remaining(remaining)
                                                                                        .percentageUsed(percentage)
                                                                                        .build();
                                                                            }));

                            String finalNextCursorTime = nextCursorTime;
                            Long finalNextCursorId = nextCursorId;
                            return responseFlux
                                    .collectList()
                                    .map(
                                            budgets ->
                                                    BudgetUsagePageResponse.builder()
                                                            .budgets(budgets)
                                                            .nextCursorTime(finalNextCursorTime)
                                                            .nextCursorId(finalNextCursorId)
                                                            .build());
                        });
    }
}
