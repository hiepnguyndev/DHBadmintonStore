package com.badminton.review.dto.response;

import java.time.*;

public record ReviewResponse(
    Long reviewId,
    Long userId,
    Long productId,
    Long orderItemId,
    Byte rating,
    String comment,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
