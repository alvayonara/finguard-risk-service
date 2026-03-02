package com.alvayonara.finguardriskservice.risk.engine;

import com.alvayonara.finguardriskservice.common.KafkaGroups;
import com.alvayonara.finguardriskservice.common.KafkaTopics;
import com.alvayonara.finguardriskservice.common.util.JsonUtil;
import com.alvayonara.finguardriskservice.transaction.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RiskEngineConsumer {

  private static final Logger log = LoggerFactory.getLogger(RiskEngineConsumer.class);

  private final RiskEngineService riskEngineService;

  public RiskEngineConsumer(RiskEngineService riskEngineService) {
    this.riskEngineService = riskEngineService;
  }

  @KafkaListener(topics = KafkaTopics.TRANSACTION_EVENTS, groupId = KafkaGroups.RISK_GROUP)
  public void consume(String message) {
    try {
      TransactionEvent event = JsonUtil.fromJson(message, TransactionEvent.class);
      switch (event.getEventType()) {
        case CREATED -> riskEngineService.handleCreated(event).subscribe();
        case UPDATED -> riskEngineService.handleUpdated(event).subscribe();
        case DELETED -> riskEngineService.handleDeleted(event).subscribe();
        default -> throw new IllegalArgumentException(
            "Unknown event type: " + event.getEventType());
      }
    } catch (Exception e) {
      log.error("Failed to process transaction event: {}", e.getMessage(), e);
    }
  }
}
