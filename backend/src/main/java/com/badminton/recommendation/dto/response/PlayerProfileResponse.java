package com.badminton.recommendation.dto.response;

import com.badminton.common.enums.PlayStyle;
import com.badminton.common.enums.PlayType;
import com.badminton.common.enums.SkillLevel;
import com.badminton.common.enums.WristStrength;
import java.math.BigDecimal;
import java.time.*;

public record PlayerProfileResponse(
    Long profileId,
    Long userId,
    SkillLevel skillLevel,
    PlayStyle playStyle,
    PlayType playType,
    WristStrength wristStrength,
    String preferredWeight,
    BigDecimal minBudget,
    BigDecimal maxBudget,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
