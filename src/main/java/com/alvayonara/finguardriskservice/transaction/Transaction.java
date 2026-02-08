package com.alvayonara.finguardriskservice.transaction;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("transactions")
public class Transaction {
    @Id
    private Long id;
    private Long userId;
    private String type;
    private BigDecimal amount;
    private String category;
    private LocalDate occurredAt;
    private LocalDateTime createdAt;
}