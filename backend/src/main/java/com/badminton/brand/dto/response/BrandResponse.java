package com.badminton.brand.dto.response;

import java.time.*;

public record BrandResponse(
    Long brandId,
    String brandName,
    String slug,
    String description,
    String logoUrl,
    com.badminton.brand.entity.BrandStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
