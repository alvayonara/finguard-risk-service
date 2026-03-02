package com.alvayonara.finguardriskservice.transaction;

import com.alvayonara.finguardriskservice.transaction.dto.CreateTransactionRequest;
import com.alvayonara.finguardriskservice.transaction.dto.TransactionResponse;
import com.alvayonara.finguardriskservice.transaction.dto.UpdateTransactionRequest;
import com.alvayonara.finguardriskservice.user.context.UserContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/transactions")
public class TransactionController {

  private final TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @PreAuthorize("hasRole('USER')")
  @PostMapping
  public Mono<TransactionResponse> createTransaction(
      @RequestBody CreateTransactionRequest request) {
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get(UserContext.CONTEXT_KEY);
          return transactionService
              .createTransaction(userContext.getInternalUserId(), request)
              .map(saved -> new TransactionResponse(saved.getId()));
        });
  }

  @PreAuthorize("hasRole('USER')")
  @PutMapping("/{id}")
  public Mono<TransactionResponse> updateTransaction(
      @PathVariable Long id, @RequestBody UpdateTransactionRequest request) {
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get(UserContext.CONTEXT_KEY);
          return transactionService
              .updateTransaction(id, userContext.getInternalUserId(), request)
              .map(saved -> new TransactionResponse(saved.getId()));
        });
  }

  @PreAuthorize("hasRole('USER')")
  @DeleteMapping("/{id}")
  public Mono<Void> deleteTransaction(@PathVariable Long id) {
    return Mono.deferContextual(
        ctx -> {
          UserContext userContext = ctx.get(UserContext.CONTEXT_KEY);
          return transactionService.deleteTransaction(id, userContext.getInternalUserId());
        });
  }
}
