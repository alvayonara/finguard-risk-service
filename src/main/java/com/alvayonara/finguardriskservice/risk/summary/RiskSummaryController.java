package com.alvayonara.finguardriskservice.risk.summary;

import com.alvayonara.finguardriskservice.user.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/risk")
public class RiskSummaryController {
  @Autowired private RiskSummaryService riskSummaryService;

  @GetMapping("/summary")
  public Mono<RiskSummaryResponse> getSummary() {
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get("userContext");
          return riskSummaryService.getSummary(userContext.getInternalUserId());
        });
  }
}
