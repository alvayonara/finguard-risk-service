package com.alvayonara.finguardriskservice.transaction;

import com.alvayonara.finguardriskservice.transaction.event.TransactionEvent;
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
              TransactionEvent event =
                  new TransactionEvent(
                      TransactionEventType.CREATED,
                      saved.getId(),
                      saved.getUserId(),
                      saved.getType(),
                      saved.getCategoryId(),
                      saved.getAmount().doubleValue(),
                      saved.getOccurredAt().toString());
              transactionEventPublisher.publish(event);
            });
  }

  public Mono<Transaction> updateTransaction(Long id, Transaction updated) {
    return transactionRepository
        .findById(id)
        .flatMap(
            existing -> {
              existing.setType(updated.getType());
              existing.setAmount(updated.getAmount());
              existing.setCategoryId(updated.getCategoryId());
              existing.setOccurredAt(updated.getOccurredAt());
              return transactionRepository.save(existing);
            })
        .doOnSuccess(
            saved -> {
              TransactionEvent event =
                  new TransactionEvent(
                      TransactionEventType.UPDATED,
                      saved.getId(),
                      saved.getUserId(),
                      saved.getType(),
                      saved.getCategoryId(),
                      saved.getAmount().doubleValue(),
                      saved.getOccurredAt().toString());
              transactionEventPublisher.publish(event);
            });
  }

  public Mono<Void> deleteTransaction(Long id) {
    return transactionRepository
        .findById(id)
        .flatMap(
            existing -> {
              TransactionEvent event =
                  new TransactionEvent(
                      TransactionEventType.DELETED,
                      existing.getId(),
                      existing.getUserId(),
                      existing.getType(),
                      existing.getCategoryId(),
                      existing.getAmount().doubleValue(),
                      existing.getOccurredAt().toString());
              transactionEventPublisher.publish(event);
              return transactionRepository.deleteById(id);
            });
  }
}
