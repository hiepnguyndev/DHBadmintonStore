package com.badminton.category.dto.response;

import java.time.*;

public record CategoryResponse(
    Long categoryId,
    String categoryName,
    String slug,
    String description,
    com.badminton.category.entity.CategoryStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
