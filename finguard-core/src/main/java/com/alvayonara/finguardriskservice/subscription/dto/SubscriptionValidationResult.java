package com.alvayonara.finguardriskservice.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SubscriptionValidationResult {
    private LocalDateTime expiry;
    private boolean canceled;
}
