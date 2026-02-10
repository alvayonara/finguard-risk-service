package com.alvayonara.finguardriskservice.risk.trend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/risk/trend")
public class RiskTrendController {
  @Autowired private RiskTrendService riskTrendService;

  @GetMapping
  public Mono<RiskTrendResponse> trend(
      @RequestParam Long userId, @RequestParam(defaultValue = "7") int days) {
    return riskTrendService.getTrend(userId, days);
  }
}
