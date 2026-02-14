package com.alvayonara.finguardriskservice.risk.insight;

import com.alvayonara.finguardriskservice.user.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/v1/risk")
public class RiskInsightController {
  @Autowired private RiskInsightService riskInsightService;

  @PreAuthorize("hasAnyRole('USER', 'ANONYMOUS')")
  @GetMapping("/insights")
  public Flux<RiskInsightResponse> getInsights() {
    return Flux.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get("userContext");
          return riskInsightService.getInsights(userContext.getInternalUserId());
        });
  }
}
