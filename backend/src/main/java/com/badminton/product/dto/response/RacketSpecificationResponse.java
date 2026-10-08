package com.badminton.product.dto.response;

import com.badminton.common.enums.BalanceType;
import com.badminton.common.enums.PlayStyle;
import com.badminton.common.enums.ShaftFlexibility;
import com.badminton.common.enums.SkillLevel;
import java.math.BigDecimal;
import java.time.*;

public record RacketSpecificationResponse(
    Long specificationId,
    Long productId,
    String weightClass,
    BalanceType balanceType,
    Integer balancePointMm,
    ShaftFlexibility shaftFlexibility,
    BigDecimal maxTensionLbs,
    PlayStyle playStyle,
    SkillLevel skillLevel,
    String gripSize,
    Integer racketLengthMm,
    String material,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
