package com.alvayonara.finguardriskservice.summary;

import com.alvayonara.finguardriskservice.common.KafkaGroups;
import com.alvayonara.finguardriskservice.common.KafkaTopics;
import com.alvayonara.finguardriskservice.common.util.JsonUtil;
import com.alvayonara.finguardriskservice.transaction.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MonthlySummaryConsumer {

  private static final Logger log = LoggerFactory.getLogger(MonthlySummaryConsumer.class);

  private final MonthlySummaryService monthlySummaryService;

  public MonthlySummaryConsumer(MonthlySummaryService monthlySummaryService) {
    this.monthlySummaryService = monthlySummaryService;
  }

  @KafkaListener(topics = KafkaTopics.TRANSACTION_EVENTS, groupId = KafkaGroups.SUMMARY_GROUP)
  public void consume(String message) {
    try {
      TransactionEvent event = JsonUtil.fromJson(message, TransactionEvent.class);
      switch (event.getEventType()) {
        case CREATED -> monthlySummaryService.handleCreated(event).subscribe();
        case UPDATED -> monthlySummaryService.handleUpdated(event).subscribe();
        case DELETED -> monthlySummaryService.handleDeleted(event).subscribe();
        default -> throw new IllegalArgumentException("Unknown event type: " + event.getEventType());
      }
    } catch (Exception e) {
      log.error("Failed to process transaction event: {}", e.getMessage(), e);
    }
  }
}
