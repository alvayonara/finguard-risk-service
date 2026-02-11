package com.alvayonara.finguardriskservice.dashboard;

import com.alvayonara.finguardriskservice.user.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/dashboard")
public class DashboardController {
  @Autowired private DashboardService dashboardService;

  @GetMapping
  public Mono<DashboardResponse> getDashboard() {
    return Mono.deferContextual(ctx -> {
      UserContext userContext = ctx.get("userContext");
      return dashboardService.getDashboard(userContext.getInternalUserId());
    });
  }
}
