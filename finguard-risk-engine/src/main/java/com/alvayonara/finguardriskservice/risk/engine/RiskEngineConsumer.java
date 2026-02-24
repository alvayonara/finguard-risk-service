package com.alvayonara.finguardriskservice.risk.engine;

import com.alvayonara.finguardriskservice.common.util.JsonUtil;
import com.alvayonara.finguardriskservice.transaction.event.TransactionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RiskEngineConsumer {
  @Autowired private RiskEngineService riskEngineService;

  @KafkaListener(topics = "transaction.events", groupId = "finguard-risk-group")
  public void consume(String message) {
    try {
      TransactionEvent event = JsonUtil.fromJson(message, TransactionEvent.class);
      switch (event.getEventType()) {
        case CREATED -> riskEngineService.handleCreated(event).subscribe();
        case UPDATED -> riskEngineService.handleUpdated(event).subscribe();
        case DELETED -> riskEngineService.handleDeleted(event).subscribe();
        default -> throw new IllegalArgumentException("Unknown event type");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
