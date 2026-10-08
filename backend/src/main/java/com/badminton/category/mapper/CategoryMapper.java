package com.badminton.category.mapper;

import com.badminton.category.dto.response.CategoryResponse;
import com.badminton.category.entity.Category;

public final class CategoryMapper {
  private CategoryMapper() {}

  public static CategoryResponse toResponse(Category e) {
    return new CategoryResponse(
        e.getCategoryId(),
        e.getCategoryName(),
        e.getSlug(),
        e.getDescription(),
        e.getStatus(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
