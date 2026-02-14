package com.alvayonara.finguardriskservice.spending.summary;

import com.alvayonara.finguardriskservice.user.context.UserContext;
import java.time.YearMonth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/spending")
public class SpendingSummaryController {
  @Autowired private SpendingSummaryService spendingSummaryService;

  @PreAuthorize("hasAnyRole('USER', 'ANONYMOUS')")
  @GetMapping("/summary")
  public Mono<SpendingSummaryResponse> getSummary(
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
    YearMonth targetMonth = month != null ? month : YearMonth.now();
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get("userContext");
          return spendingSummaryService.getSummary(userContext.getInternalUserId(), targetMonth);
        });
  }
}
