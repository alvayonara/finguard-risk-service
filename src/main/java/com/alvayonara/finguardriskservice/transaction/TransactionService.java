package com.alvayonara.finguardriskservice.transaction;

import com.alvayonara.finguardriskservice.transaction.event.TransactionCreatedEvent;
import com.alvayonara.finguardriskservice.transaction.event.TransactionEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TransactionService {
  @Autowired private TransactionRepository transactionRepository;
  @Autowired private TransactionEventPublisher transactionEventPublisher;

  public Mono<Transaction> createTransaction(Transaction tx) {
    return transactionRepository
        .save(tx)
        .doOnSuccess(
            saved -> {
              TransactionCreatedEvent event =
                  new TransactionCreatedEvent(
                      saved.getId(),
                      saved.getUserId(),
                      saved.getType(),
                      saved.getCategoryId(),
                      saved.getAmount().doubleValue(),
                      saved.getOccurredAt().toString());
              transactionEventPublisher.publishTransactionCreated(event);
            });
  }
}
