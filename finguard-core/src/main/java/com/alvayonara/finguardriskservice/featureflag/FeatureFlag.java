package com.alvayonara.finguardriskservice.featureflag;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@Table("app_feature_flags")
public class FeatureFlag {
    @Id
    private Long id;
    private String flagKey;
    private Boolean enabled;
    private Integer rolloutPercentage;
    private String requiredPlan;
    private String platform;
    private String minAppVersion;
    private String description;
    private LocalDateTime updatedAt;
}