package com.alvayonara.finguardriskservice.activity;

import com.alvayonara.finguardriskservice.activity.dto.ActivityResponse;
import com.alvayonara.finguardriskservice.user.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/activity")
public class ActivityController {
  @Autowired private ActivityService activityService;

  @PreAuthorize("hasAnyRole('USER', 'ANONYMOUS')")
  @GetMapping
  public Mono<ActivityResponse> getActivities(
      @RequestParam(required = false) String cursorTime,
      @RequestParam(required = false) Long cursorId,
      @RequestParam(defaultValue = "20") int limit) {
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get("userContext");
          return activityService.getActivities(
              userContext.getInternalUserId(), cursorTime, cursorId, limit);
        });
  }
}
