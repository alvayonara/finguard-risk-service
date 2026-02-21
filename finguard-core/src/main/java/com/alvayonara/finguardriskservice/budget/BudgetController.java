package com.alvayonara.finguardriskservice.budget;

import com.alvayonara.finguardriskservice.budget.dto.BudgetRequest;
import com.alvayonara.finguardriskservice.budget.dto.BudgetUsagePageResponse;
import com.alvayonara.finguardriskservice.budget.dto.BudgetUsageResponse;
import com.alvayonara.finguardriskservice.user.context.UserContext;
import java.time.YearMonth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/budgets")
public class BudgetController {
  @Autowired private BudgetService budgetService;

  @PreAuthorize("hasRole('USER')")
  @GetMapping
  public Mono<BudgetUsagePageResponse> getBudgets(
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
      @RequestParam(required = false) String cursorTime,
      @RequestParam(required = false) Long cursorId,
      @RequestParam(defaultValue = "10") int limit) {
    YearMonth yearMonth = month != null ? month : YearMonth.now();
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get("userContext");
          return budgetService.getMonthlyBudgetUsagePaginated(
              userContext.getInternalUserId(), yearMonth, cursorTime, cursorId, limit);
        });
  }

  @PreAuthorize("hasRole('USER')")
  @PostMapping
  public Mono<BudgetConfig> createOrUpdate(@RequestBody BudgetRequest request) {
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get("userContext");
          return budgetService.upsertBudget(
              userContext.getInternalUserId(), request.getCategoryId(), request.getMonthlyLimit());
        });
  }

  @PreAuthorize("hasRole('USER')")
  @DeleteMapping("/{categoryId}")
  public Mono<Void> deleteBudget(@PathVariable Long categoryId) {
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get("userContext");
          return budgetService.deleteBudget(userContext.getInternalUserId(), categoryId);
        });
  }
}
