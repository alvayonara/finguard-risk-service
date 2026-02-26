package com.alvayonara.finguardriskservice.summary;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@Table("monthly_summary")
public class MonthlySummary {
    @Id
    private Long id;
    private Long userId;
    private String monthKey;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private LocalDateTime updatedAt;
}
