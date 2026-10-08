package com.badminton.product.dto.request;

import com.badminton.common.enums.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

public record RacketRequest(
    @NotBlank @Pattern(regexp = "[2-6]U") String weightClass,
    @NotNull BalanceType balanceType,
    @Min(200) @Max(400) Integer balancePointMm,
    @NotNull ShaftFlexibility shaftFlexibility,
    @DecimalMin("1") @DecimalMax("99") BigDecimal maxTensionLbs,
    @NotNull PlayStyle playStyle,
    @NotNull SkillLevel skillLevel,
    @Size(max = 20) String gripSize,
    @Min(400) @Max(900) Integer racketLengthMm,
    @Size(max = 255) String material) {}
