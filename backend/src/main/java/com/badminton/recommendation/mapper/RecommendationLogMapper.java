package com.badminton.recommendation.mapper;

import com.badminton.recommendation.dto.response.RecommendationLogResponse;
import com.badminton.recommendation.entity.RecommendationLog;

public final class RecommendationLogMapper {
  private RecommendationLogMapper() {}

  public static RecommendationLogResponse toResponse(RecommendationLog e) {
    return new RecommendationLogResponse(
        e.getLogId(),
        e.getUser() == null ? null : e.getUser().getUserId(),
        e.getProduct() == null ? null : e.getProduct().getProductId(),
        e.getMatchScore(),
        e.getReasonJson(),
        e.getCreatedAt());
  }
}
