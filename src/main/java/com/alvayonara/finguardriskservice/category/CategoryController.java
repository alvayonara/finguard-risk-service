package com.alvayonara.finguardriskservice.category;

import com.alvayonara.finguardriskservice.user.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/categories")
public class CategoryController {
  @Autowired private CategoryService service;

  @GetMapping
  public Flux<CategoryResponse> getAll() {
    return Flux.deferContextual(
        ctx -> {
          UserContext user = ctx.get("userContext");
          return service.getAll(user.getInternalUserId());
        });
  }

  @PostMapping
  public Mono<CategoryResponse> create(@RequestBody CategoryRequest request) {
    return Mono.deferContextual(
        ctx -> {
          UserContext user = ctx.get("userContext");
          return service.create(user.getInternalUserId(), request);
        });
  }

  @DeleteMapping("/{id}")
  public Mono<Void> delete(@PathVariable Long id) {
    return Mono.deferContextual(
        ctx -> {
          UserContext user = ctx.get("userContext");
          return service.delete(user.getInternalUserId(), id);
        });
  }
}
