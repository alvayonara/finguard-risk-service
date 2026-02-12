package com.alvayonara.finguardriskservice.summary.spending;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryAmount {
  private String category;
  private BigDecimal amount;
}
