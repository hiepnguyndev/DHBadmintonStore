package com.badminton.recommendation.mapper;

import com.badminton.recommendation.dto.response.PlayerProfileResponse;
import com.badminton.recommendation.entity.PlayerProfile;

public final class PlayerProfileMapper {
  private PlayerProfileMapper() {}

  public static PlayerProfileResponse toResponse(PlayerProfile e) {
    return new PlayerProfileResponse(
        e.getProfileId(),
        e.getUser() == null ? null : e.getUser().getUserId(),
        e.getSkillLevel(),
        e.getPlayStyle(),
        e.getPlayType(),
        e.getWristStrength(),
        e.getPreferredWeight(),
        e.getMinBudget(),
        e.getMaxBudget(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
