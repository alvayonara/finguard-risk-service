package com.alvayonara.finguardriskservice.budget;

import com.alvayonara.finguardriskservice.budget.dto.BudgetRequest;
import com.alvayonara.finguardriskservice.budget.dto.BudgetUsageResponse;
import com.alvayonara.finguardriskservice.user.context.UserContext;
import java.time.YearMonth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/budgets")
public class BudgetController {
  @Autowired private BudgetService budgetService;

  @GetMapping
  public Flux<BudgetUsageResponse> getBudgets(
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
    YearMonth target = month != null ? month : YearMonth.now();
    return Flux.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get("userContext");
          return budgetService.getMonthlyBudgetUsage(userContext.getInternalUserId(), target);
        });
  }

  @PostMapping
  public Mono<BudgetConfig> createOrUpdate(@RequestBody BudgetRequest request) {
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get("userContext");
          return budgetService.upsertBudget(
              userContext.getInternalUserId(), request.getCategoryId(), request.getMonthlyLimit());
        });
  }
}
