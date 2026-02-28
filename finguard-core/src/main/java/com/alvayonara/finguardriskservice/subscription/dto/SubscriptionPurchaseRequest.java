package com.alvayonara.finguardriskservice.subscription.dto;

import jakarta.validation.constraints.NotBlank;

public record SubscriptionPurchaseRequest(
    @NotBlank String platform, @NotBlank String productId, @NotBlank String transactionData) {}
