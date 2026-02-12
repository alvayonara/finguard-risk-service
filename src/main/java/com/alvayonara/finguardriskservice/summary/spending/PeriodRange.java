package com.alvayonara.finguardriskservice.summary.spending;

import java.time.LocalDate;
import java.time.YearMonth;

public record PeriodRange(LocalDate start, LocalDate end) {

  public static PeriodRange of(YearMonth month) {
    LocalDate start = month.atDay(1);
    LocalDate end = month.plusMonths(1).atDay(1);
    return new PeriodRange(start, end);
  }
}
