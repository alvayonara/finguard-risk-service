package com.alvayonara.finguardriskservice.transaction.event;

import com.alvayonara.finguardriskservice.common.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventPublisher {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private JsonUtil jsonUtil;

    public void publishTransactionCreated(TransactionCreatedEvent event) {
        try {
            String payload = jsonUtil.toJson(event);
            kafkaTemplate.send("transaction.created", payload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
