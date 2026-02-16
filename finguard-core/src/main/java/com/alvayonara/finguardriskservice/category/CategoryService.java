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
        .findByUserIdAndName(userId, request.getName())
        .flatMap(
            existing -> Mono.<Category>error(new IllegalStateException("Category already exists")))
        .switchIfEmpty(repository.save(buildCategory(userId, request)))
        .map(this::toResponse);
  }

  public Mono<Void> delete(Long userId, Long id) {
    return repository
        .findById(id)
        .filter(category -> !category.getIsDefault())
        .filter(category -> category.getUserId().equals(userId))
        .flatMap(repository::delete);
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
