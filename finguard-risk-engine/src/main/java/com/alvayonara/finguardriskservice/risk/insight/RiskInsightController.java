package com.alvayonara.finguardriskservice.risk.insight;

import com.alvayonara.finguardriskservice.user.context.UserContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/v1/risk")
public class RiskInsightController {

  private final RiskInsightService riskInsightService;

  public RiskInsightController(RiskInsightService riskInsightService) {
    this.riskInsightService = riskInsightService;
  }

  @PreAuthorize("hasRole('USER')")
  @GetMapping("/insights")
  public Flux<RiskInsightResponse> getInsights() {
    return Flux.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get(UserContext.CONTEXT_KEY);
          return riskInsightService.getInsights(userContext.getInternalUserId());
        });
  }
}
