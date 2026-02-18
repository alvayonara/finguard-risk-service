package com.alvayonara.finguardriskservice.subscription.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AppleReceiptResponse {
    /**
     * 0 = valid
     * 21007 = sandbox receipt sent to production
     * 21008 = production receipt sent to sandbox
     */
    private int status;
    private String environment;
    @JsonProperty("is-retryable")
    private Boolean retryable;
    @JsonProperty("latest_receipt_info")
    private List<LatestReceiptInfo> latestReceiptInfo;
    @JsonProperty("pending_renewal_info")
    private List<PendingRenewalInfo> pendingRenewalInfo;

    @Data
    public static class LatestReceiptInfo {
        @JsonProperty("product_id")
        private String productId;
        @JsonProperty("transaction_id")
        private String transactionId;
        @JsonProperty("original_transaction_id")
        private String originalTransactionId;
        @JsonProperty("purchase_date_ms")
        private String purchaseDateMs;
        @JsonProperty("expires_date_ms")
        private String expiresDateMs;
        @JsonProperty("is_trial_period")
        private String isTrialPeriod;
        @JsonProperty("is_in_intro_offer_period")
        private String isIntroOfferPeriod;
        @JsonProperty("cancellation_date_ms")
        private String cancellationDateMs;
    }

    @Data
    public static class PendingRenewalInfo {
        @JsonProperty("product_id")
        private String productId;
        @JsonProperty("auto_renew_status")
        private String autoRenewStatus;
        @JsonProperty("auto_renew_product_id")
        private String autoRenewProductId;
        @JsonProperty("expiration_intent")
        private String expirationIntent;
    }
}