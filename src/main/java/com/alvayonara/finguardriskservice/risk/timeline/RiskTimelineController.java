package com.alvayonara.finguardriskservice.risk.timeline;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/risk/timeline")
public class RiskTimelineController {
  @Autowired private RiskTimelineService riskTimelineService;

  @GetMapping
  public Mono<RiskTimelineResponse> getTimeline(
      @RequestParam Long userId,
      @RequestParam(required = false) String cursorTime,
      @RequestParam(required = false) Long cursorId,
      @RequestParam(defaultValue = "10") int limit) {
    return riskTimelineService.getTimeline(userId, cursorTime, cursorId, limit);
  }
}
