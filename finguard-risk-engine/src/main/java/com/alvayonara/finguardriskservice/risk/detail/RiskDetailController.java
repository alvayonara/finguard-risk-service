package com.alvayonara.finguardriskservice.risk.detail;

import com.alvayonara.finguardriskservice.user.context.UserContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/risk")
public class RiskDetailController {

  private final RiskDetailService riskDetailService;

  public RiskDetailController(RiskDetailService riskDetailService) {
    this.riskDetailService = riskDetailService;
  }

  @PreAuthorize("hasRole('USER')")
  @GetMapping("/detail")
  public Mono<RiskDetailResponse> detail() {
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get(UserContext.CONTEXT_KEY);
          return riskDetailService.getDetail(userContext.getInternalUserId());
        });
  }
}
