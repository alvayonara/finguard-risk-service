package com.alvayonara.finguardriskservice.transaction;

import com.alvayonara.finguardriskservice.transaction.dto.CreateTransactionRequest;
import com.alvayonara.finguardriskservice.transaction.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/transactions")
public class TransactionController {
  @Autowired private TransactionService transactionService;

  @PostMapping
  public Mono<TransactionResponse> createTransaction(
      @RequestBody CreateTransactionRequest request) {
    Transaction tx = new Transaction();
    tx.setUserId(request.getUserId());
    tx.setType(request.getType());
    tx.setAmount(request.getAmount());
    tx.setCategory(request.getCategory());
    tx.setOccurredAt(request.getOccurredAt());
    return transactionService
        .createTransaction(tx)
        .map(saved -> new TransactionResponse(saved.getId()));
  }
}
