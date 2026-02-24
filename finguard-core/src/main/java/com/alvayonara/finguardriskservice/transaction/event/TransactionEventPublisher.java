package com.alvayonara.finguardriskservice.transaction.event;

import com.alvayonara.finguardriskservice.common.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventPublisher {
  @Autowired private KafkaTemplate<String, String> kafkaTemplate;

  private static final String TOPIC = "transaction.events";

  public void publish(TransactionEvent event) {
    try {
      String payload = JsonUtil.toJson(event);
      kafkaTemplate.send(TOPIC, payload);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
