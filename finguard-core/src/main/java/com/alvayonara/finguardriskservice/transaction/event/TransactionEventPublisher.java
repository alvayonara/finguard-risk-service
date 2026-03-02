package com.alvayonara.finguardriskservice.transaction.event;

import com.alvayonara.finguardriskservice.common.KafkaTopics;
import com.alvayonara.finguardriskservice.common.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventPublisher {

  private static final Logger log = LoggerFactory.getLogger(TransactionEventPublisher.class);

  private final KafkaTemplate<String, String> kafkaTemplate;

  public TransactionEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void publish(TransactionEvent event) {
    try {
      String payload = JsonUtil.toJson(event);
      kafkaTemplate.send(KafkaTopics.TRANSACTION_EVENTS, payload);
    } catch (Exception e) {
      log.error("Failed to publish transaction event: {}", e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }
}
