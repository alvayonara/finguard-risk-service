package com.alvayonara.finguardriskservice.risk.event;

import com.alvayonara.finguardriskservice.common.KafkaTopics;
import com.alvayonara.finguardriskservice.common.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class RiskEventProducer {

  private static final Logger log = LoggerFactory.getLogger(RiskEventProducer.class);

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public RiskEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void publishRiskLevelChanged(RiskLevelChangedEvent event) {
    try {
      String payload = JsonUtil.toJson(event);
      kafkaTemplate.send(KafkaTopics.RISK_LEVEL_CHANGED, event.getUserId().toString(), payload);
    } catch (Exception e) {
      log.error("Failed to publish risk level changed event: {}", e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }
}
