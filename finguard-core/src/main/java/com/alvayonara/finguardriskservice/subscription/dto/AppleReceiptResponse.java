package com.alvayonara.finguardriskservice.subscription.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AppleReceiptResponse {
    private int status;
    @JsonProperty("latest_receipt_info")
    private List<LatestReceiptInfo> latestReceiptInfoList;

    @Data
    public static class LatestReceiptInfo {
        private String product_id;
        @JsonProperty("expires_date_ms")
        private String expiresDateMs;
    }
}

