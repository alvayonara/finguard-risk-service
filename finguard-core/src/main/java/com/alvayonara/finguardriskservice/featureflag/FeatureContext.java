package com.alvayonara.finguardriskservice.featureflag;

public record FeatureContext(String userUid, String userPlan, String platform, String appVersion) {}
