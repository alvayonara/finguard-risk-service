package com.alvayonara.finguardriskservice.summary;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table("monthly_summary")
public class MonthlySummary {
    @Id
    private Long userId;
    private String yearMonth;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private LocalDateTime updatedAt;
}
