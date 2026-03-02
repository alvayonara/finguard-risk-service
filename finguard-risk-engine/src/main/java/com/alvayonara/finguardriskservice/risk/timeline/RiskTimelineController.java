package com.alvayonara.finguardriskservice.risk.timeline;

import com.alvayonara.finguardriskservice.user.context.UserContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/risk/timeline")
public class RiskTimelineController {

  private final RiskTimelineService riskTimelineService;

  public RiskTimelineController(RiskTimelineService riskTimelineService) {
    this.riskTimelineService = riskTimelineService;
  }

  @PreAuthorize("hasRole('USER')")
  @GetMapping
  public Mono<RiskTimelineResponse> getTimeline(
      @RequestParam(required = false) String cursorTime,
      @RequestParam(required = false) Long cursorId,
      @RequestParam(defaultValue = "10") int limit) {
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get(UserContext.CONTEXT_KEY);
          return riskTimelineService.getTimeline(
              userContext.getInternalUserId(), cursorTime, cursorId, limit);
        });
  }
}
