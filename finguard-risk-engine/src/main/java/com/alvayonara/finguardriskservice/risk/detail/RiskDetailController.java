package com.alvayonara.finguardriskservice.risk.detail;

import com.alvayonara.finguardriskservice.user.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/risk")
public class RiskDetailController {
  @Autowired private RiskDetailService riskDetailService;

  @PreAuthorize("hasAnyRole('USER', 'ANONYMOUS')")
  @GetMapping("/detail")
  public Mono<RiskDetailResponse> detail() {
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get("userContext");
          return riskDetailService.getDetail(userContext.getInternalUserId());
        });
  }
}
