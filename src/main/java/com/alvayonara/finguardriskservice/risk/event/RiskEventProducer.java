package com.alvayonara.finguardriskservice.risk.event;

import com.alvayonara.finguardriskservice.common.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class RiskEventProducer {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private JsonUtil jsonUtil;

    public void publishRiskLevelChanged(RiskLevelChangedEvent event) {
        try {
            String payload = jsonUtil.toJson(event);
            kafkaTemplate.send("risk.level.changed", event.getUserId().toString(), payload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
