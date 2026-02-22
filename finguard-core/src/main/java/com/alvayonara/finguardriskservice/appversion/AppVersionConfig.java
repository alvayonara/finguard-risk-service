package com.alvayonara.finguardriskservice.appversion;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@Table("app_version_config")
public class AppVersionConfig {
    @Id
    private Long id;
    private String platform;
    private String minSupportedVersion;
    private String latestVersion;
    private Boolean forceUpdate;
    private Boolean maintenanceMode;
    private String maintenanceMessage;
    private String storeUrl;
    private LocalDateTime updatedAt;
}