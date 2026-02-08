package com.alvayonara.finguardriskservice.risk.engine;

import com.alvayonara.finguardriskservice.common.util.JsonUtil;
import com.alvayonara.finguardriskservice.transaction.event.TransactionCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RiskEngineConsumer {
    @Autowired
    private RiskEngineService riskEngineService;
    @Autowired
    private JsonUtil jsonUtil;

    @KafkaListener(topics = "transaction.created", groupId = "finguard-risk-group")
    public void consume(String message) {
        try {
            TransactionCreatedEvent event = jsonUtil.fromJson(message, TransactionCreatedEvent.class);
            riskEngineService.evaluate(event).subscribe();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
