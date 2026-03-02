package com.alvayonara.finguardriskservice.activity;

import com.alvayonara.finguardriskservice.activity.dto.*;
import com.alvayonara.finguardriskservice.category.Category;
import com.alvayonara.finguardriskservice.category.CategoryRepository;
import com.alvayonara.finguardriskservice.risk.insight.RiskInsightResponse;
import com.alvayonara.finguardriskservice.risk.insight.RiskInsightService;
import com.alvayonara.finguardriskservice.risk.level.history.RiskLevelHistory;
import com.alvayonara.finguardriskservice.risk.level.history.RiskLevelHistoryRepository;
import com.alvayonara.finguardriskservice.transaction.Transaction;
import com.alvayonara.finguardriskservice.transaction.TransactionRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ActivityService {

  private final TransactionRepository transactionRepository;
  private final RiskLevelHistoryRepository riskLevelHistoryRepository;
  private final RiskInsightService riskInsightService;
  private final CategoryRepository categoryRepository;

  public ActivityService(
      TransactionRepository transactionRepository,
      RiskLevelHistoryRepository riskLevelHistoryRepository,
      RiskInsightService riskInsightService,
      CategoryRepository categoryRepository) {
    this.transactionRepository = transactionRepository;
    this.riskLevelHistoryRepository = riskLevelHistoryRepository;
    this.riskInsightService = riskInsightService;
    this.categoryRepository = categoryRepository;
  }

  public Mono<ActivityResponse> getActivities(
      Long userId, String cursorTime, Long cursorId, int limit) {
    Flux<Transaction> transactionFlux;
    Flux<RiskLevelHistory> riskChangeFlux;

    if (Objects.isNull(cursorTime)) {
      transactionFlux = transactionRepository.findFirstPageByUserId(userId, limit);
      riskChangeFlux = riskLevelHistoryRepository.findFirstPageByUserId(userId, limit);
    } else {
      LocalDateTime cursor = LocalDateTime.parse(cursorTime);
      transactionFlux = transactionRepository.findNextPageByUserId(userId, cursor, cursorId, limit);
      riskChangeFlux =
          riskLevelHistoryRepository.findNextPageByUserId(userId, cursor, cursorId, limit);
    }

    Mono<List<Transaction>> transactionsMono = transactionFlux.collectList();
    Mono<List<RiskLevelHistory>> riskChangesMono = riskChangeFlux.collectList();
    Mono<List<RiskInsightResponse>> insightsMono =
        riskInsightService.getInsights(userId).collectList();

    return Mono.zip(transactionsMono, riskChangesMono, insightsMono)
        .flatMap(
            tuple -> {
              List<Transaction> transactions = tuple.getT1();
              List<RiskLevelHistory> riskChanges = tuple.getT2();
              List<RiskInsightResponse> insights = tuple.getT3();

              List<Long> categoryIds =
                  transactions.stream().map(Transaction::getCategoryId).distinct().toList();

              if (categoryIds.isEmpty()) {
                return Mono.just(
                    buildResponse(transactions, riskChanges, insights, Map.of(), limit));
              }

              return categoryRepository
                  .findAllById(categoryIds)
                  .collectMap(Category::getId)
                  .map(
                      categoryMap ->
                          buildResponse(transactions, riskChanges, insights, categoryMap, limit));
            })
        .switchIfEmpty(Mono.just(new ActivityResponse(null, List.of(), null, null)));
  }

  private ActivityResponse buildResponse(
      List<Transaction> transactions,
      List<RiskLevelHistory> riskChanges,
      List<RiskInsightResponse> insights,
      Map<Long, Category> categoryMap,
      int limit) {

    List<ActivityItemWrapper> allItems = new ArrayList<>();

    for (Transaction tx : transactions) {
      var category = categoryMap.get(tx.getCategoryId());
      if (category != null) {
        TransactionActivityData data =
            new TransactionActivityData(
                tx.getId(),
                tx.getType().name(),
                tx.getAmount(),
                new TransactionActivityData.CategoryInfo(
                    category.getId(), category.getName(), category.getIcon(), category.getColor()),
                tx.getOccurredAt(),
                tx.getCreatedAt());

        allItems.add(
            new ActivityItemWrapper(ActivityType.TRANSACTION, data, tx.getCreatedAt(), tx.getId()));
      }
    }

    for (RiskLevelHistory risk : riskChanges) {
      RiskChangeActivityData data =
          new RiskChangeActivityData(
              risk.getOldLevel(),
              risk.getNewLevel(),
              risk.getTopSignalType(),
              risk.getOccurredAt());

      allItems.add(
          new ActivityItemWrapper(
              ActivityType.RISK_CHANGE, data, risk.getOccurredAt(), risk.getId()));
    }

    Collections.sort(allItems);

    List<ActivityResponse.ActivityItem> responseItems =
        allItems.stream()
            .limit(limit)
            .map(wrapper -> new ActivityResponse.ActivityItem(wrapper.getType(), wrapper.getData()))
            .collect(Collectors.toList());

    ActivityItemWrapper lastItem = allItems.size() >= limit ? allItems.get(limit - 1) : null;
    String nextCursorTime = lastItem != null ? lastItem.getTimestamp().toString() : null;
    Long nextCursorId = lastItem != null ? lastItem.getId() : null;

    InsightActivityData topInsight = null;
    if (!insights.isEmpty()) {
      RiskInsightResponse insight = insights.getFirst();
      topInsight =
          new InsightActivityData(insight.getType(), insight.getMessage(), insight.getSeverity());
    }

    return new ActivityResponse(topInsight, responseItems, nextCursorTime, nextCursorId);
  }
}
