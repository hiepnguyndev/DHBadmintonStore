package com.badminton.product.dto.response;

import java.time.*;

public record ProductImageResponse(
    Long imageId,
    Long productId,
    String imageUrl,
    Integer sortOrder,
    Boolean isPrimary,
    LocalDateTime createdAt) {}
