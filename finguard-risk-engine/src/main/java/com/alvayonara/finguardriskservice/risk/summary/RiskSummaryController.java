package com.alvayonara.finguardriskservice.risk.summary;

import com.alvayonara.finguardriskservice.user.context.UserContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/risk")
public class RiskSummaryController {

  private final RiskSummaryService riskSummaryService;

  public RiskSummaryController(RiskSummaryService riskSummaryService) {
    this.riskSummaryService = riskSummaryService;
  }

  @PreAuthorize("hasRole('USER')")
  @GetMapping("/summary")
  public Mono<RiskSummaryResponse> getSummary() {
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get(UserContext.CONTEXT_KEY);
          return riskSummaryService.getSummary(userContext.getInternalUserId());
        });
  }
}
