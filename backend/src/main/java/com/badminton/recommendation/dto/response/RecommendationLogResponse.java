package com.badminton.recommendation.dto.response;

import java.math.BigDecimal;
import java.time.*;

public record RecommendationLogResponse(
    Long logId,
    Long userId,
    Long productId,
    BigDecimal matchScore,
    String reasonJson,
    LocalDateTime createdAt) {}
