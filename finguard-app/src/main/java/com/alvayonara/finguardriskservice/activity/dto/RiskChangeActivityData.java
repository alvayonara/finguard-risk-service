package com.alvayonara.finguardriskservice.activity.dto;

import java.time.LocalDateTime;

public record RiskChangeActivityData(
    String previousLevel, String currentLevel, String topSignalType, LocalDateTime changedAt) {}
