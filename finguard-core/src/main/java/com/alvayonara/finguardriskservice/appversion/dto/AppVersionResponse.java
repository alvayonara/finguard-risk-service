package com.alvayonara.finguardriskservice.appversion.dto;

public record AppVersionResponse(
        String minSupportedVersion,
        String latestVersion,
        boolean forceUpdate,
        boolean maintenanceMode,
        String maintenanceMessage,
        String storeUrl,
        boolean mustUpdate
) {
}