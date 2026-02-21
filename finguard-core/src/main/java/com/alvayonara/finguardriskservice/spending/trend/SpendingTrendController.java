package com.alvayonara.finguardriskservice.spending.trend;

import com.alvayonara.finguardriskservice.user.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/spending")
public class SpendingTrendController {
  @Autowired private SpendingTrendService spendingTrendService;

  @PreAuthorize("hasRole('USER')")
  @GetMapping("/trend")
  public Mono<SpendingTrendResponse> getTrend(@RequestParam(defaultValue = "6") int months) {
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get("userContext");
          return spendingTrendService.getTrend(userContext.getInternalUserId(), months);
        });
  }
}
