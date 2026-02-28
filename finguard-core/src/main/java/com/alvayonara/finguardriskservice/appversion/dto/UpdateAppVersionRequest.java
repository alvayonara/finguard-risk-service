package com.alvayonara.finguardriskservice.appversion.dto;

public record UpdateAppVersionRequest(
    String minSupportedVersion,
    String latestVersion,
    boolean forceUpdate,
    boolean maintenanceMode,
    String maintenanceMessage,
    String storeUrl) {}
