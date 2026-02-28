package com.alvayonara.finguardriskservice.appversion;

import com.alvayonara.finguardriskservice.appversion.dto.AppVersionResponse;
import com.alvayonara.finguardriskservice.appversion.dto.UpdateAppVersionRequest;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AppVersionService {
  @Autowired private AppVersionRepository repository;

  public Mono<AppVersionResponse> checkVersion(String platform, String clientVersion) {
    return repository
        .findByPlatform(platform.toUpperCase())
        .switchIfEmpty(Mono.error(new RuntimeException("Platform config not found")))
        .map(
            config -> {
              boolean mustUpdate = isLowerVersion(clientVersion, config.getMinSupportedVersion());
              return new AppVersionResponse(
                  config.getMinSupportedVersion(),
                  config.getLatestVersion(),
                  config.getForceUpdate(),
                  config.getMaintenanceMode(),
                  config.getMaintenanceMessage(),
                  config.getStoreUrl(),
                  mustUpdate);
            });
  }

  public Mono<AppVersionConfig> update(String platform, UpdateAppVersionRequest request) {
    return repository
        .findByPlatform(platform.toUpperCase())
        .flatMap(
            config -> {
              config.setMinSupportedVersion(request.minSupportedVersion());
              config.setLatestVersion(request.latestVersion());
              config.setForceUpdate(request.forceUpdate());
              config.setMaintenanceMode(request.maintenanceMode());
              config.setMaintenanceMessage(request.maintenanceMessage());
              config.setStoreUrl(request.storeUrl());
              config.setUpdatedAt(LocalDateTime.now());
              return repository.save(config);
            });
  }

  private boolean isLowerVersion(String client, String minimum) {
    String[] clientParts = client.split("\\.");
    String[] minParts = minimum.split("\\.");
    int length = Math.max(clientParts.length, minParts.length);
    for (int i = 0; i < length; i++) {
      int clientPart = i < clientParts.length ? Integer.parseInt(clientParts[i]) : 0;
      int minPart = i < minParts.length ? Integer.parseInt(minParts[i]) : 0;
      if (clientPart < minPart) {
        return true;
      }
      if (clientPart > minPart) {
        return false;
      }
    }
    return false;
  }
}
