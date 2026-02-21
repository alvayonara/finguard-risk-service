package com.alvayonara.finguardriskservice.risk.trend;

import com.alvayonara.finguardriskservice.user.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/risk/trend")
public class RiskTrendController {
  @Autowired private RiskTrendService riskTrendService;

  @PreAuthorize("hasRole('USER')")
  @GetMapping
  public Mono<RiskTrendResponse> trend(@RequestParam(defaultValue = "7") int days) {
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get("userContext");
          return riskTrendService.getTrend(userContext.getInternalUserId(), days);
        });
  }
}
