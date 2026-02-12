package com.alvayonara.finguardriskservice.user.preference;

import com.alvayonara.finguardriskservice.user.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/user/preferences")
public class UserPreferenceController {
  @Autowired private UserPreferenceService service;

  @GetMapping
  public Mono<UserPreferenceResponse> get() {
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get("userContext");
          return service.get(userContext.getInternalUserId());
        });
  }

  @PutMapping
  public Mono<Void> update(@RequestBody UserPreferenceRequest request) {
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get("userContext");
          return service.update(userContext.getInternalUserId(), request);
        });
  }
}
