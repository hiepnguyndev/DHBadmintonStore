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
    @NotNull @DecimalMin("0") @Digits(integer = 13, fraction = 2) BigDecimal maxBudget) {}
