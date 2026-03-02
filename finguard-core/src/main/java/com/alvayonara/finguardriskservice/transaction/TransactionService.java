package com.alvayonara.finguardriskservice.transaction;

import com.alvayonara.finguardriskservice.transaction.dto.CreateTransactionRequest;
import com.alvayonara.finguardriskservice.transaction.dto.UpdateTransactionRequest;
import com.alvayonara.finguardriskservice.transaction.event.TransactionEvent;
import com.alvayonara.finguardriskservice.transaction.event.TransactionEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TransactionService {

  private final TransactionRepository transactionRepository;
  private final TransactionEventPublisher transactionEventPublisher;

  public TransactionService(
      TransactionRepository transactionRepository,
      TransactionEventPublisher transactionEventPublisher) {
    this.transactionRepository = transactionRepository;
    this.transactionEventPublisher = transactionEventPublisher;
  }

  public Mono<Transaction> createTransaction(Long userId, CreateTransactionRequest request) {
    Transaction tx = new Transaction();
    tx.setUserId(userId);
    tx.setType(request.getType());
    tx.setAmount(request.getAmount());
    tx.setCategoryId(request.getCategoryId());
    tx.setOccurredAt(request.getOccurredAt());
    return transactionRepository
        .save(tx)
        .doOnSuccess(saved -> publishEvent(TransactionEventType.CREATED, saved));
  }

  public Mono<Transaction> updateTransaction(
      Long id, Long userId, UpdateTransactionRequest request) {
    return transactionRepository
        .findById(id)
        .switchIfEmpty(Mono.error(new RuntimeException("Transaction not found")))
        .flatMap(
            existing -> {
              if (!existing.getUserId().equals(userId)) {
                return Mono.error(new RuntimeException("Forbidden"));
              }
              existing.setType(request.getType());
              existing.setAmount(request.getAmount());
              existing.setCategoryId(request.getCategoryId());
              existing.setOccurredAt(request.getOccurredAt());
              return transactionRepository.save(existing);
            })
        .doOnSuccess(saved -> publishEvent(TransactionEventType.UPDATED, saved));
  }

  public Mono<Void> deleteTransaction(Long id, Long userId) {
    return transactionRepository
        .findById(id)
        .switchIfEmpty(Mono.error(new RuntimeException("Transaction not found")))
        .flatMap(
            existing -> {
              if (!existing.getUserId().equals(userId)) {
                return Mono.error(new RuntimeException("Forbidden"));
              }
              publishEvent(TransactionEventType.DELETED, existing);
              return transactionRepository.delete(existing);
            });
  }

  private void publishEvent(TransactionEventType type, Transaction tx) {
    TransactionEvent event =
        new TransactionEvent(
            type,
            tx.getId(),
            tx.getUserId(),
            tx.getType(),
            tx.getCategoryId(),
            tx.getAmount().doubleValue(),
            tx.getOccurredAt().toString());
    transactionEventPublisher.publish(event);
  }
}
