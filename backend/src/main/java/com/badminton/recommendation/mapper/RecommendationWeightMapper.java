package com.badminton.recommendation.mapper;

import com.badminton.recommendation.dto.response.RecommendationWeightResponse;
import com.badminton.recommendation.entity.RecommendationWeight;

public final class RecommendationWeightMapper {
  private RecommendationWeightMapper() {}

  public static RecommendationWeightResponse toResponse(RecommendationWeight e) {
    return new RecommendationWeightResponse(
        e.getWeightId(),
        e.getSkillWeight(),
        e.getPlayStyleWeight(),
        e.getWristWeight(),
        e.getWeightClassWeight(),
        e.getFlexWeight(),
        e.getBudgetWeight(),
        e.getIsActive(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
