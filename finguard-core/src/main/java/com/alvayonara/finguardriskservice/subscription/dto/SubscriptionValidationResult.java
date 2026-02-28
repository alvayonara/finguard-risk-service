package com.alvayonara.finguardriskservice.subscription.dto;

import java.time.LocalDateTime;

public record SubscriptionValidationResult(
    String productId, LocalDateTime expiry, boolean autoRenew, boolean canceled) {}
