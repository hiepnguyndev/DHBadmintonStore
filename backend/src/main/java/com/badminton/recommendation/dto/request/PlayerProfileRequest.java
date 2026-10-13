package com.badminton.recommendation.dto.request;

import com.badminton.common.enums.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

public record PlayerProfileRequest(
    @NotNull SkillLevel skillLevel,
    @NotNull PlayStyle playStyle,
    @NotNull PlayType playType,
    @NotNull WristStrength wristStrength,
    @NotBlank @Pattern(regexp = "[2-6]U") String preferredWeight,
    @NotNull @DecimalMin("0") @Digits(integer = 13, fraction = 2) BigDecimal minBudget,
    @NotNull @DecimalMin("0") @Digits(integer = 13, fraction = 2) BigDecimal maxBudget,
    @Pattern(regexp = "^(|RIGHT|LEFT)$") String dominantHand,
    @Pattern(regexp = "^(|HEAD_HEAVY|EVEN_BALANCE|HEAD_LIGHT)$") String balanceType,
    @Pattern(regexp = "^(|FLEXIBLE|MEDIUM|STIFF|EXTRA_STIFF)$") String shaftFlexibility,
    @Size(max = 12) String shirtSize,
    @Size(max = 12) String pantsSize,
    @Pattern(regexp = "^(|3[5-9]|4[0-8])$") String shoeSize,
    @Size(max = 255) String favoriteColors,
    @Size(max = 255) String favoriteBrands) {}
