package com.alvayonara.finguardriskservice.risk.summary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/risk")
public class RiskSummaryController {
    @Autowired
    private RiskSummaryService riskSummaryService;

    @GetMapping("/summary")
    public Mono<RiskSummaryResponse> getSummary(@RequestParam Long userId) {
        return riskSummaryService.getSummary(userId);
    }
}
