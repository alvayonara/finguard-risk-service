package com.alvayonara.finguardriskservice.risk.level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/risk")
public class RiskLevelController {
  @Autowired private RiskLevelService riskLevelService;

  @GetMapping("/level")
  public Mono<RiskLevelResponse> getRiskLevel(@RequestParam Long userId) {
    return riskLevelService.getRiskLevel(userId);
  }
}
