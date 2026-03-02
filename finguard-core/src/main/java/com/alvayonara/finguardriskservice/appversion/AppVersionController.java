package com.alvayonara.finguardriskservice.appversion;

import com.alvayonara.finguardriskservice.appversion.dto.AppVersionResponse;
import com.alvayonara.finguardriskservice.appversion.dto.UpdateAppVersionRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/app")
public class AppVersionController {

  private final AppVersionService service;

  public AppVersionController(AppVersionService service) {
    this.service = service;
  }

  @GetMapping("/version")
  public Mono<AppVersionResponse> checkVersion(
      @RequestParam String platform, @RequestParam String version) {
    return service.checkVersion(platform, version);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/version/{platform}")
  public Mono<AppVersionConfig> updateVersion(
      @PathVariable String platform, @RequestBody UpdateAppVersionRequest request) {
    return service.update(platform, request);
  }
}
