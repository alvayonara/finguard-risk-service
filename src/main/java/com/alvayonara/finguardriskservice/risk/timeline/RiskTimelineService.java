package com.alvayonara.finguardriskservice.risk.timeline;

import com.alvayonara.finguardriskservice.risk.level.history.RiskLevelHistory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RiskTimelineService {
  @Autowired private RiskTimelineRepository riskTimelineRepository;

  public Mono<RiskTimelineResponse> getTimeline(
      Long userId, String cursorTime, Long cursorId, int limit) {
    Flux<RiskLevelHistory> source;
    if (Objects.isNull(cursorTime)) {
      source = riskTimelineRepository.findFirstPage(userId, limit);
    } else {
      source =
          riskTimelineRepository.findNextPage(
              userId, LocalDateTime.parse(cursorTime), cursorId, limit);
    }
    return source
        .collectList()
        .map(
            list -> {
              List<RiskTimelineItem> items =
                  list.stream()
                      .map(
                          h ->
                              RiskTimelineItem.builder()
                                  .level(h.getNewLevel())
                                  .signal(h.getTopSignalType())
                                  .occurredAt(h.getOccurredAt())
                                  .build())
                      .toList();
              RiskLevelHistory last = list.isEmpty() ? null : list.get(list.size() - 1);
              return RiskTimelineResponse.builder()
                  .items(items)
                  .nextCursorTime(last != null ? last.getOccurredAt().toString() : null)
                  .nextCursorId(last != null ? last.getId() : null)
                  .build();
            });
  }
}
