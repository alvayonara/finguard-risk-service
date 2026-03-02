package com.alvayonara.finguardriskservice.risk.level;

import com.alvayonara.finguardriskservice.user.context.UserContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/risk")
public class RiskLevelController {

  private final RiskLevelService riskLevelService;

  public RiskLevelController(RiskLevelService riskLevelService) {
    this.riskLevelService = riskLevelService;
  }

  @PreAuthorize("hasRole('USER')")
  @GetMapping("/level")
  public Mono<RiskLevelResponse> getRiskLevel() {
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get(UserContext.CONTEXT_KEY);
          return riskLevelService.getRiskLevel(userContext.getInternalUserId());
        });
  }
}
