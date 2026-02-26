package com.alvayonara.finguardriskservice.summary;

import com.alvayonara.finguardriskservice.common.util.JsonUtil;
import com.alvayonara.finguardriskservice.transaction.event.TransactionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MonthlySummaryConsumer {

    @Autowired
    private MonthlySummaryService monthlySummaryService;

    @KafkaListener(topics = "transaction.events", groupId = "finguard-summary-group")
    public void consume(String message) {
        try {
            TransactionEvent event = JsonUtil.fromJson(message, TransactionEvent.class);
            switch (event.getEventType()) {
                case CREATED -> monthlySummaryService.handleCreated(event).subscribe();
                case UPDATED -> monthlySummaryService.handleUpdated(event).subscribe();
                case DELETED -> monthlySummaryService.handleDeleted(event).subscribe();
                default -> throw new IllegalArgumentException("Unknown event type");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
