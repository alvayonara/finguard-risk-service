package com.alvayonara.finguardriskservice.category;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CategoryService {
  @Autowired private CategoryRepository repository;

  public Flux<CategoryResponse> getAll(Long userId) {
    return repository
        .findAllByUser(userId)
        .map(
            category ->
                CategoryResponse.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .type(category.getType())
                    .icon(category.getIcon())
                    .color(category.getColor())
                    .isDefault(category.getIsDefault())
                    .build());
  }

  public Mono<CategoryResponse> create(Long userId, CategoryRequest request) {
    return repository
        .findByUserOrDefaultAndNameAndType(userId, request.getName(), request.getType())
        .flatMap(
            existing ->
                Mono.<Category>error(
                    new DuplicateCategoryException(request.getName(), request.getType())))
        .switchIfEmpty(repository.save(buildCategory(userId, request)))
        .map(this::toResponse);
  }

  public Mono<CategoryResponse> update(Long userId, Long id, CategoryRequest request) {
    return repository
        .findByIdAndUserId(id, userId)
        .switchIfEmpty(
            Mono.error(new IllegalStateException("Category not found or not authorized")))
        .flatMap(
            existing ->
                repository
                    .findByUserOrDefaultAndNameAndType(userId, request.getName(), request.getType())
                    .flatMap(
                        duplicate -> {
                          if (!duplicate.getId().equals(id)) {
                            return Mono.error(
                                new DuplicateCategoryException(
                                    request.getName(), request.getType()));
                          }
                          return Mono.just(existing);
                        })
                    .switchIfEmpty(Mono.just(existing)))
        .flatMap(
            category -> {
              category.setName(request.getName());
              category.setType(request.getType());
              category.setIcon(request.getIcon());
              category.setColor(request.getColor());
              return repository.save(category);
            })
        .map(this::toResponse);
  }

  public Mono<Void> delete(Long userId, Long id) {
    return repository
        .findById(id)
        .filter(category -> !category.getIsDefault())
        .filter(category -> category.getUserId().equals(userId))
        .flatMap(
            category ->
                repository
                    .findOtherDefaultCategory(category.getType())
                    .flatMap(
                        otherCategory -> {
                          reassignTransactionsInBatches(id, otherCategory.getId()).subscribe();
                          return repository.delete(category);
                        }));
  }

  private Mono<Void> reassignTransactionsInBatches(Long oldCategoryId, Long newCategoryId) {
    final int BATCH_SIZE = 100;
    return Mono.defer(() -> processNextBatch(oldCategoryId, newCategoryId, 0L, BATCH_SIZE));
  }

  private Mono<Void> processNextBatch(
      Long oldCategoryId, Long newCategoryId, Long lastId, int batchSize) {
    return repository
        .updateTransactionCategoriesBatch(newCategoryId, oldCategoryId, lastId, batchSize)
        .flatMap(
            updatedCount -> {
              if (updatedCount == 0) {
                return Mono.empty();
              }
              return repository
                  .findTransactionIdsByCategoryIdAfter(newCategoryId, lastId, batchSize)
                  .takeLast(1)
                  .next()
                  .flatMap(
                      newLastId ->
                          processNextBatch(oldCategoryId, newCategoryId, newLastId, batchSize))
                  .switchIfEmpty(Mono.empty());
            })
        .then();
  }

  private Category buildCategory(Long userId, CategoryRequest request) {
    Category category = new Category();
    category.setUserId(userId);
    category.setName(request.getName());
    category.setType(request.getType());
    category.setIcon(request.getIcon());
    category.setColor(request.getColor());
    category.setIsDefault(false);
    category.setCreatedAt(LocalDateTime.now());
    return category;
  }

  private CategoryResponse toResponse(Category category) {
    return CategoryResponse.builder()
        .id(category.getId())
        .name(category.getName())
        .type(category.getType())
        .icon(category.getIcon())
        .color(category.getColor())
        .isDefault(category.getIsDefault())
        .build();
  }
}
