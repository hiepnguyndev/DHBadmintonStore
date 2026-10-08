package com.badminton.recommendation.dto.response;

import java.math.BigDecimal;
import java.time.*;

public record RecommendationWeightResponse(
    Long weightId,
    BigDecimal skillWeight,
    BigDecimal playStyleWeight,
    BigDecimal wristWeight,
    BigDecimal weightClassWeight,
    BigDecimal flexWeight,
    BigDecimal budgetWeight,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
