package com.alvayonara.finguardriskservice.transaction.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateTransactionRequest {
    private Long userId;
    private String type;
    private BigDecimal amount;
    private String category;
    private LocalDate occurredAt;
}
